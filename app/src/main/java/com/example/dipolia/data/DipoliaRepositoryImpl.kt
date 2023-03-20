package com.example.dipolia.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.database.ColorList
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.network.*
import com.example.dipolia.data.workers.RefreshSendUDPWorker
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.Horn
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import kotlinx.coroutines.delay


class DipoliaRepositoryImpl(private val application: Application) : DipoliaRepository {
//object DipoliaRepositoryImpl : DipoliaRepository {

    private val dipolsDao = AppDatabase.getInstance(application).dipolsDao()
    private val mapper = DipoliaMapper()

    private val receiver = UDPServer()
    private val sender = UDPClient()

    private val lampEntityListNetworkResult = mutableListOf<LampDomainEntity>()




    override suspend fun sendFollowMe() {
        while (true) {
            sender.sendUDPSuspend("Follow me")
            delay(1000)
        }
    }

    override suspend fun receiveLocalModeData() {

//        val lampEntityListNetworkResult = mutableListOf<LampDomainEntity>()

        while (true) {

            val receivedDipolData = receiver.receiveStringAndIPFromUDP()
            Log.d("receiveLocalModeData", "Pair received: $receivedDipolData")

            receivedDipolData?.let {

                val ar = it.first.split(" ")
                val lampTypeString = ar[0]
                Log.d("receiveLocalModeData", "lampTypeString = $lampTypeString")

                if (lampTypeString == "dipol" || lampTypeString == "5lights") {

                    Log.d("receiveLocalModeData", "inside if lampTypeString = $lampTypeString")
                    val id = ar[1].substring(0, ar[1].length - 1)
                    var already = 0

                    for (lamp in lampEntityListNetworkResult) {
                        if (lamp.id == id) {
// connected list control:
                            lamp.lastConnection = System.currentTimeMillis() / 1000
                            Log.d(
                                "TEST",
                                "i.lastConnection = ${lamp.id} ${lamp.lastConnection} ${lamp.connected}"
                            )
//                            Log.d(
//                                "TEST",
//                                "currentTime - 5 = ${System.currentTimeMillis() / 1000 - 5}"
//                            )
                            already = 1
                            break
                        }
                    }
                    Log.d(
                        "TEST",
                        "List controlled = ${lampEntityListNetworkResult.map { item -> item.id to item.connected }}"
                    )

                    if (already == 0) {
                        val lampType = when (lampTypeString) {
                            "dipol" -> LampType.DIPOl
                            "5lights" -> LampType.FIVE_LIGHTS
                            else -> LampType.UNKNOWN_LAMP_TYPE
                        }
                        val lampDto = LampDto(id, it.second, lampType, System.currentTimeMillis()/1000)
//                        Log.d("UDP receiveLocalModeData", "dipol $lampDto added")
//                        Log.d("UDP receiveLocalModeData", "dipolListDto $lampListDto")

                        val itemToListEntity = mapper.mapLampDtoToEntity(lampDto)
                        val itemToAdd = mapper.mapLampDtoToDbModel(lampDto)
//                        Log.d("UDP receiveLocalModeData", "itemToAdd = $itemToAdd")
                        val itemFromDb = dipolsDao.getLampItemById(lampDto.id)
//                        Log.d("UDP receiveLocalModeData", "itemFromDb = $itemFromDb")
                        if (itemFromDb == null) {
                            dipolsDao.addLampItem(itemToAdd)
                        } else {
                            itemToListEntity.c = itemFromDb.colorList
                            Log.d("TEST", "itemToListEntity.c = ${itemToListEntity.c}")
                        }
                        lampEntityListNetworkResult.add(itemToListEntity)
                        Log.d(
                            "TEST",
                            "lampEntityListNetworkResult = ${lampEntityListNetworkResult.map { item -> item.id to item.connected }}"
                        )
                    }
                }
            }
        }
    }


    override suspend fun refreshConnectedList() {
//        val notConnectedList = dipolsDao.getLampsList()
//        val refreshedList = notConnectedList
//            .filter { it.connected }
//            .map { it.copy(connected = false) }
//        for (lamp in refreshedList) {
//            dipolsDao.updateLampItem(lamp)
//        }
    }

    override suspend fun dipolsConnectionMonitoring() {
//        while (true) {
//            val notConnectedList = dipolsDao.getNotConnectedLampsList()
//            for (lamp in notConnectedList) {
//                dipolsDao.updateLampItem(lamp.copy(connected = false))
//            }
//            delay(1000)
//        }
    }


    override fun getSelectedDipol(): LiveData<DipolDomainEntity?> {
        return Transformations.map(dipolsDao.getSelectedDipolItemLD(LampType.DIPOl)) { it ->
            it?.let {
                mapper.mapLampDbModelToDipolEntity(it)
            }
        }
    }

    override fun getConnectedFiveLights(): LiveData<FiveLightsDomainEntity?> {
        return Transformations.map(dipolsDao.getConnectedLampsListByTypeLD(LampType.FIVE_LIGHTS)) { it ->
//            Log.d("getFiveLights", "$it")
            it?.let {
                if (it.isNotEmpty()) {
                    mapper.mapLampDbModelToFiveLightsEntity(it[0])
                } else {
                    null
                }
            }

        }
    }

    override fun unselectDipol() {
        val selectedDipol = dipolsDao.getSelectedDipolItem(true)
//        selectedDipol?.let { dipolsDao.updateDipolItem(it.copy(selected = false)) }
        selectedDipol?.let { dipolsDao.updateDipolItem(it.copy()) }
    }

    override fun unselectLamp() {
//        val selectedLamp = dipolsDao.getLampSelectedItem(true)
        val selectedLamp = dipolsDao.getLampSelectedItem()
//        selectedLamp?.let { dipolsDao.updateLampItem(it.copy(selected = false)) }
        selectedLamp?.let { dipolsDao.updateLampItem(it.copy()) }

    }

    override fun getSelectedConnectedLampType(): LiveData<LampType?> {
        return Transformations.map(
//            dipolsDao.getLampSelectedConnectedItemLD(selected = true)
            dipolsDao.getLampSelectedConnectedItemLD()
        ) {
            it?.lampType
        }
    }

    override fun workerStartStop() {
        val workManager = WorkManager.getInstance(application)
        val workInfoLF = workManager.getWorkInfosForUniqueWork(RefreshSendUDPWorker.WORK_NAME)
        val workInfo = workInfoLF.get()
        if (workInfo.isNotEmpty() && workInfo[0].state.toString() == "RUNNING") {
//            Log.d("onClick workerStartStop", "workerState == \"RUNNING\"")
            workManager.cancelAllWork()
        } else {
//            Log.d("onClick workerStartStop", "workerState == \"CANCELED\"")
            workManager.enqueueUniqueWork(
                RefreshSendUDPWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,  //what to do, if another worker will be started
                RefreshSendUDPWorker.makeRequest()
            )
        }
    }

    override fun getIsBroadcast(): LiveData<Boolean?> {

        val workManager = WorkManager.getInstance(application)
        val infoLD = workManager.getWorkInfosForUniqueWorkLiveData(RefreshSendUDPWorker.WORK_NAME)
//        Log.d("getIsBroadcast", "infoLD = $infoLD")
        return Transformations.map(infoLD) {
//            Log.d("getIsBroadcast", "$it")
            it.isNotEmpty() && it[0].state.toString() == "RUNNING"
        }
    }

    override fun getLampsTable(): LiveData<List<LampDomainEntity>> {
        return Transformations.map(dipolsDao.getLampsTable()) { it ->
            it.map {
                mapper.mapLampDbModelToEntity(it)
            }
        }
    }

    override fun getSelectedLamp(): LiveData<LampDomainEntity?> {

//        return Transformations.map(dipolsDao.getLampSelectedItemLD(true)) { it ->
        return Transformations.map(dipolsDao.getLampSelectedItemLD()) { it ->
            it?.let {
                mapper.mapLampDbModelToEntity(it)
            }
        }
    }


    override fun testSendLocalModeData() {

        val workManager = WorkManager.getInstance(application)
        workManager.enqueueUniqueWork(
            RefreshSendUDPWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,  //what to do, if another worker will be started
            RefreshSendUDPWorker.makeRequest()
        )
//    Log.d("RefreshSendUDPWorker", "makeOneTimeRequest")

//        workManager.enqueueUniquePeriodicWork(
//            RefreshSendUDPWorker.WORK_NAME,
//            ExistingPeriodicWorkPolicy.REPLACE,  //what to do, if another worker will be started
//            RefreshSendUDPWorker.makePeriodicRequest()
//        )
    }


    override fun getConnectedDipolList(): LiveData<List<DipolDomainEntity>> {
        return Transformations.map(dipolsDao.getConnectedLampsListByTypeLD(LampType.DIPOl)) { it ->
//            Log.d("getDipolList", "$it")
            it.map {
                mapper.mapLampDbModelToDipolEntity(it)
            }
        }
    }

    override fun selectLamp(lampId: String) {
        Log.d("onClickListener", " SelectedItem: $lampId")

//        var oldSelectedItem = dipolsDao.getLampSelectedItem(true)
        var oldSelectedItem = dipolsDao.getLampSelectedItem()
        Log.d(
            "onItemClickListener",
//            " oldSelectedItem: ${oldSelectedItem?.lampId} ${oldSelectedItem?.selected}"
            " oldSelectedItem: ${oldSelectedItem?.lampId} "
        )

        var newSelectedItem = dipolsDao.getLampItemById(lampId)
        Log.d(
            "onItemClickListener",
//            " newSelectedItem: ${newSelectedItem?.lampId} ${newSelectedItem?.selected}"
            " newSelectedItem: ${newSelectedItem?.lampId}"
        )

//        newSelectedItem?.let {
//            if (oldSelectedItem?.lampId != it.lampIp) {
//                val oldSelectedItemToUpdate = oldSelectedItem?.copy(selected = false)
//                Log.d(
//                    "onItemClickListener",
//                    " oldSelectedItemToUpdate: ${oldSelectedItemToUpdate?.lampId} ${oldSelectedItemToUpdate?.selected}"
//                )
//
//                oldSelectedItemToUpdate?.let { item ->
//                    while (dipolsDao.getLampItemById(item.lampId)?.selected == true) {
//                        val rowsOldsUpdated = dipolsDao.updateLampItem(item)
//                        Log.d(
//                            "onItemClickListener",
//                            "SelectedItem:  rowsOldsUpdated  $rowsOldsUpdated"
//                        )
//                        Log.d(
//                            "onItemClickListener",
//                            "SelectedItem:  dipolsDao.getLampItemById  ${
//                                dipolsDao.getLampItemById(item.lampId)?.selected
//                            }"
//                        )
//                    }
//                }
//                val newSelectedItemToUpdate = it.copy(selected = true)
//                Log.d(
//                    "onItemClickListener",
//                    " newSelectedItemToUpdate: ${newSelectedItemToUpdate.lampId} ${newSelectedItemToUpdate.selected}"
//                )
//                while (dipolsDao.getLampItemById(newSelectedItem.lampId)?.selected == false) {
//                    val rowsNewsUpdated = dipolsDao.updateLampItem(newSelectedItemToUpdate)
//                    Log.d("onItemClickListener", "SelectedItem: rowsNewsUpdated  $rowsNewsUpdated")
//                }
//            }
//        }
    }

    override fun changeLocalState(set: String, index: Int, value: Double) {
        Log.d("DipoliaRepositoryImpl", "changeLocalState $set $index $value")
        if (set == "dipol") {
//            val dipolItem = dipolsDao.getLampSelectedItem(true)
            val dipolItem = dipolsDao.getLampSelectedItem()
            Log.d("DipoliaRepositoryImpl", "changeLocalState $dipolItem")

            dipolItem?.let {
                if (it.lampType == LampType.DIPOl) {            // some crutch
                    var colorList = dipolItem.colorList.colors.toMutableList()
                    if (colorList.isEmpty()) {
                        colorList = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                    }
//                Log.d("changeLocalState", "colorList $colorList")
                    colorList[index] =
                        value                    // here was crush before crutch. IndexOutOfBoundsException: Index: 5, Size: 5
                    val newDipolItem = dipolItem.copy(colorList = ColorList(colorList))

                    dipolsDao.updateLampItem(newDipolItem)
//                Log.d("DipoliaRepositoryImpl", "changeLocalState newDipolItem $newDipolItem")}
                }
            }
        } else if (set == "fiveLights") {
//            val fiveLightsItem = dipolsDao.getLampSelectedItem(true)
            val fiveLightsItem = dipolsDao.getLampSelectedItem()
//            Log.d("DipoliaRepositoryImpl", "changeLocalState $fiveLightsItem")

            fiveLightsItem?.let {
                if (it.lampType == LampType.FIVE_LIGHTS) {            // second crutch
                    var colorList = fiveLightsItem.colorList.colors.toMutableList()
                    if (colorList.isEmpty()) {
                        colorList = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0)
                    }
//                Log.d("changeLocalState", "colorList $colorList")
                    colorList[index] = value
                    val newFiveLightsItem = fiveLightsItem.copy(colorList = ColorList(colorList))

                    dipolsDao.updateLampItem(newFiveLightsItem)
//                Log.d("DipoliaRepositoryImpl", "changeLocalState newFiveLightsItem $newFiveLightsItem")
                }
            }
        }
    }


    override fun updateLocalStateList(idStateList: List<Pair<String, String>>) {
        TODO("Not yet implemented")
    }

    override suspend fun editDipolItem(dipolDomainEntity: DipolDomainEntity) {
        TODO()
    }

    override fun changeGlobalState(horn: Horn, colorDiff: Double) {
        TODO("Not yet implemented")
    }

}