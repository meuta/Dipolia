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
import com.example.dipolia.data.network.DipolDto
import com.example.dipolia.data.network.LampDto
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.data.network.UDPServer
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

    override suspend fun sendFollowMe() {
        while (true) {
            sender.sendUDPSuspend("Follow me")
            delay(50)
        }
    }

    override suspend fun receiveLocalModeData() {

        val lampListDto = mutableListOf<LampDto>()
        while (true) {
            val receivedDipolData = receiver.receiveStringAndIPFromUDP()
//            Log.d("UDP receiveLocalModeData", "Pair received: $receivedDipolData")

            receivedDipolData?.let {
//                Log.d("UDP receiveLocalModeData", "let")

                val ar = it.first.split(" ")
                val lampTypeString = ar[0]
//                Log.d("UDP receiveLocalModeData", "lampTypeString = $lampTypeString")

                if (lampTypeString == "dipol" || lampTypeString == "5lights") {
                    val id = ar[1].substring(0, ar[1].length - 1)
                    var already = 0


                    for (i in lampListDto) {
                        if (i.id == id) {
// connected list control:
                            val myLamp = dipolsDao.getLampItemById(id)
                            myLamp?.let { lamp ->
                                val timeString = System.currentTimeMillis() / 1000
                                dipolsDao.updateLampItem(
                                    lamp.copy(
                                        connected = true,
                                        lastConnection = timeString
                                    )
                                )
                                Log.d(
                                    "dipolsDao.updateLampItem",
                                    "${lamp.lampId} ${lamp.connected} ${lamp.selected} ${lamp.lastConnection}"
                                )
                            }

                            already = 1
                            break
                        }
                    }

                    if (already == 0) {
                        val lampType = when (lampTypeString) {
                            "dipol" -> LampType.DIPOl
                            "5lights" -> LampType.FIVE_LIGHTS
                            else -> LampType.UNKNOWN_LAMP_TYPE
                        }
                        val lampDto = LampDto(id, it.second, lampType, it.first)
                        lampListDto.add(lampDto)
//                        Log.d("UDP receiveLocalModeData", "dipol $lampDto added")
//                        Log.d("UDP receiveLocalModeData", "dipolListDto $lampListDto")

                        val itemToAdd = mapper.mapLampDtoToDbModel(lampDto)
//                        Log.d("UDP receiveLocalModeData", "itemToAdd = $itemToAdd")
                        val itemFromDb = dipolsDao.getLampItemById(lampDto.id)
//                        Log.d("UDP receiveLocalModeData", "itemFromDb = $itemFromDb")
                        if (itemFromDb == null) {
                            dipolsDao.addLampItem(itemToAdd)
                        } else {
                            val itemToAddFromDb = itemFromDb.copy(connected = true)
//                            Log.d("UDP receiveLocalModeData", "itemToAddFromDb = $itemToAddFromDb")
                            dipolsDao.addLampItem(itemToAddFromDb)
                        }
                    }
                }
            }
//            delay(10)
        }
    }


    override suspend fun refreshConnectedList() {
        val notConnectedList = dipolsDao.getLampsList()
        val refreshedList = notConnectedList
            .filter { it.connected }
            .map { it.copy(connected = false) }
        for (lamp in refreshedList) {
            dipolsDao.updateLampItem(lamp)
        }
    }

    override suspend fun dipolsConnectionMonitoring() {
        while (true) {
            val notConnectedList = dipolsDao.getNotConnectedLampsList()
            for (lamp in notConnectedList) {
                dipolsDao.updateLampItem(lamp.copy(connected = false))
            }
            delay(1000)
        }
    }


    override fun getSelectedDipol(): LiveData<DipolDomainEntity?> {
        return Transformations.map(dipolsDao.getSelectedDipolItemLD(true, LampType.DIPOl)) { it ->
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
                } else{
                    null
                }
            }

        }
    }

    override fun unselectDipol() {
        val selectedDipol = dipolsDao.getSelectedDipolItem(true)
        selectedDipol?.let { dipolsDao.updateDipolItem(it.copy(selected = false)) }
    }

    override fun unselectLamp() {
            val selectedLamp = dipolsDao.getLampSelectedItem(true)
            selectedLamp?.let { dipolsDao.updateLampItem(it.copy(selected = false)) }

    }

    override fun getSelectedConnectedLampType(): LiveData<LampType?> {
        return Transformations.map(dipolsDao.getLampSelectedConnectedItemLD(
            selected = true,
            connected = true
        )){
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

            return Transformations.map(dipolsDao.getLampSelectedItemLD(true)) { it ->
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

        var oldSelectedItem = dipolsDao.getLampSelectedItem(true)
        Log.d("onItemClickListener", " oldSelectedItem: ${oldSelectedItem?.lampId} ${oldSelectedItem?.selected}")

        var newSelectedItem = dipolsDao.getLampItemById(lampId)
        Log.d("onItemClickListener", " newSelectedItem: ${newSelectedItem?.lampId} ${newSelectedItem?.selected}")

        newSelectedItem?.let {
            if (oldSelectedItem?.lampId != it.lampIp) {
                val oldSelectedItemToUpdate = oldSelectedItem?.copy(selected = false)
                Log.d("onItemClickListener", " oldSelectedItemToUpdate: ${oldSelectedItemToUpdate?.lampId} ${oldSelectedItemToUpdate?.selected}")

                oldSelectedItemToUpdate?.let {item ->
                    val rowsOldsUpdated1 = dipolsDao.updateLampItem(item)
                    val rowsOldsUpdated2 = dipolsDao.updateLampItem(item)
                    val rowsOldsUpdated3 = dipolsDao.updateLampItem(item)
//                    Log.d("onItemClickListener", " Dao updateSelectedItem: ${item.lampId} ${item.selected}")
                    Log.d("onItemClickListener", "SelectedItem: rowsOldsUpdated  $rowsOldsUpdated1 $rowsOldsUpdated2 $rowsOldsUpdated3")
                }
                val newSelectedItemToUpdate = it.copy(selected = true)
                Log.d("onItemClickListener", " newSelectedItemToUpdate: ${newSelectedItemToUpdate.lampId} ${newSelectedItemToUpdate.selected}")
                val rowsNewsUpdated1 = dipolsDao.updateLampItem(newSelectedItemToUpdate)
                val rowsNewsUpdated2 = dipolsDao.updateLampItem(newSelectedItemToUpdate)
                val rowsNewsUpdated3 = dipolsDao.updateLampItem(newSelectedItemToUpdate)
                Log.d("onItemClickListener", "SelectedItem: rowsNewsUpdated $rowsNewsUpdated1 $rowsNewsUpdated2 $rowsNewsUpdated3 ")

            }
        }
    }

    override fun changeLocalState(set: String, index: Int, value: Double) {
//        Log.d("DipoliaRepositoryImpl", "changeLocalState $set $index $value")
        if (set == "dipol") {
            val dipolItem = dipolsDao.getLampSelectedItem(true)
//            Log.d("DipoliaRepositoryImpl", "changeLocalState $dipolItem")

            dipolItem?.let {
                var colorList = dipolItem.colorList.colors.toMutableList()
                if (colorList.isEmpty()) {
                    colorList = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                }
//                Log.d("changeLocalState", "colorList $colorList")
                colorList[index] = value
                val newDipolItem = dipolItem.copy(colorList = ColorList(colorList))

                dipolsDao.updateLampItem(newDipolItem)
//                Log.d("DipoliaRepositoryImpl", "changeLocalState newDipolItem $newDipolItem")
            }
        } else if (set == "fiveLights") {
            val fiveLightsItem = dipolsDao.getLampSelectedItem(true)
//            Log.d("DipoliaRepositoryImpl", "changeLocalState $fiveLightsItem")

            fiveLightsItem?.let {
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