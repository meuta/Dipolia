package com.example.dipolia.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.dipolia.data.database.AppDatabase
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

        val dipolListDto = mutableListOf<DipolDto>()
        var fiveLights: DipolDto = DipolDto("", sender.getInetAddressByName(""), "")

        val lampListDto = mutableListOf<LampDto>()
        while (true) {
            val receivedDipolData = receiver.receiveStringAndIPFromUDP()
            Log.d("UDP receiveLocalModeData", "Pair received: $receivedDipolData")

            receivedDipolData?.let {
//                Log.d("UDP receiveLocalModeData", "let")

                val ar = it.first.split(" ")
                val lampTypeString = ar[0]
                Log.d("UDP receiveLocalModeData", "lampTypeString = $lampTypeString")

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
                                    "${lamp.lampId} ${lamp.connected} ${lamp.lastConnection}"
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
                        val lampDto = LampDto(
                            id,
                            it.second,
                            lampType,
                            it.first
                        )
                        lampListDto.add(lampDto)
                        Log.d("UDP receiveLocalModeData", "dipol $lampDto added")
                        Log.d("UDP receiveLocalModeData", "dipolListDto $lampListDto")

                        val itemToAdd = mapper.mapLampDtoToDbModel(lampDto)
                        Log.d("UDP receiveLocalModeData", "itemToAdd = $itemToAdd")
                        val itemFromDb = dipolsDao.getLampItemById(lampDto.id)
                        Log.d("UDP receiveLocalModeData", "itemFromDb = $itemFromDb")
                        if (itemFromDb == null) {
                            dipolsDao.addLampItem(itemToAdd)
                        } else {
                            val itemToAddFromDb = itemFromDb.copy(connected = true)
                            Log.d("UDP receiveLocalModeData", "itemToAddFromDb = $itemToAddFromDb")
                            dipolsDao.addLampItem(itemToAddFromDb)
                        }
                    }
                }
            }
        }
    }


    override suspend fun refreshConnectedList() {
        val notConnectedList = dipolsDao.getDipolList()
        val refreshedList = notConnectedList
            .filter { it.connected }
            .map { it.copy(connected = false) }
        for (dipol in refreshedList) {
            dipolsDao.updateDipolItem(dipol)
        }
    }

    override suspend fun dipolsConnectionMonitoring() {
        while (true) {
            val notConnectedList = dipolsDao.getNotConnectedDipolList()

            for (dipol in notConnectedList) {
                dipolsDao.updateDipolItem(dipol.copy(connected = false))
            }
            val notConnectedFiveLights = dipolsDao.getNotConnectedFiveLight()
            notConnectedFiveLights?.let {
                dipolsDao.updateFiveLightsItem(notConnectedFiveLights.copy(connected = false))
            }
            delay(1000)
        }

    }


    override fun getSelectedDipol(): LiveData<DipolDomainEntity?> {
        return Transformations.map(dipolsDao.getSelectedDipolItemLD(true)) { it ->
            it?.let {
                mapper.mapDbModelToEntity(it)
            }
        }
    }

    override fun getFiveLights(): LiveData<FiveLightsDomainEntity?> {
        return Transformations.map(dipolsDao.getFiveLightsByTypeLD(LampType.FIVE_LIGHTS)) { it ->
            Log.d("getFiveLights", "$it")
            it?.let {
                mapper.mapLampDbModelToFiveLightsEntity(it)
            }
        }
    }

    override fun unselectDipol() {
        val selectedDipol = dipolsDao.getSelectedDipolItem(true)
        selectedDipol?.let { dipolsDao.updateDipolItem(it.copy(selected = false)) }
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
            Log.d("getDipolList", "$it")
            it.map {
                mapper.mapLampDbModelToDipolEntity(it)
            }
        }
    }

    override fun selectDipolItem(dipolId: String) {
        Log.d("onClickListener", " SelectedItem: $dipolId")

//            var oldSelectedItem = dipolsDao.getSelectedDipolItem(true)
        var oldSelectedItem = dipolsDao.getLampSelectedItem(true)
        Log.d("onItemClickListener", " oldSelectedItem: ${oldSelectedItem?.lampId}")

        var newSelectedItem = dipolsDao.getLampItemById(dipolId)
        Log.d("onItemClickListener", " newSelectedItem: ${newSelectedItem?.lampId}")

        newSelectedItem?.let {
            if (oldSelectedItem?.lampId != it.lampIp) {
                oldSelectedItem = oldSelectedItem?.copy(selected = false)
                Log.d("onItemClickListener", " oldSelectedItemCopied: ${oldSelectedItem?.lampId}")

                oldSelectedItem?.let {
                    dipolsDao.updateLampItem(it)
                }
                val item = it.copy(selected = true)
                Log.d("onDipolItemClickListener", " newSelectedItemCopied: ${it.lampId}")
                dipolsDao.updateLampItem(item)
            }
        }


    }


    //    override fun changeLocalState(
//        dipolItem: DipolDomainEntity,
//        horn: Horn,
//        component: ColorComponent,
//        componentDiff: Double
//    ) {
    override fun changeLocalState(set: String, index: Int, value: Double) {
        Log.d("DipoliaRepositoryImpl", "changeLocalState $set $index $value")
        if (set == "dipol") {
            val oldDipolItem = dipolsDao.getSelectedDipolItem(true)
            Log.d("DipoliaRepositoryImpl", "changeLocalState $oldDipolItem")

            oldDipolItem?.let {
                val newDipolItem = when (index) {
                    0 -> oldDipolItem.copy(r1 = value)
                    1 -> oldDipolItem.copy(g1 = value)
                    2 -> oldDipolItem.copy(b1 = value)
                    3 -> oldDipolItem.copy(r2 = value)
                    4 -> oldDipolItem.copy(g2 = value)
                    5 -> oldDipolItem.copy(b2 = value)
                    else -> throw Exception("seekBarIndex is out of range")
                }
                dipolsDao.updateDipolItem(newDipolItem)
                Log.d("DipoliaRepositoryImpl", "changeLocalState newDipolItem $newDipolItem")
            }
        } else if (set == "fiveLights") {
            val oldFiveLightsItem = dipolsDao.getFiveLightsItemById("b4e62d52abc2")
            Log.d("DipoliaRepositoryImpl", "changeLocalState $oldFiveLightsItem")

            oldFiveLightsItem?.let {
                val newFiveLightsItem = when (index) {
                    0 -> oldFiveLightsItem.copy(r = value)
                    1 -> oldFiveLightsItem.copy(g = value)
                    2 -> oldFiveLightsItem.copy(b = value)
                    3 -> oldFiveLightsItem.copy(w = value)
                    4 -> oldFiveLightsItem.copy(uv = value)
                    else -> throw Exception("seekBarIndex is out of range")
                }
                dipolsDao.updateFiveLightsItem(newFiveLightsItem)
                Log.d(
                    "DipoliaRepositoryImpl",
                    "changeLocalState newFiveLightsItem $newFiveLightsItem"
                )
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