package com.example.dipolia.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.network.DipolDto
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.data.network.UDPServer
import com.example.dipolia.data.workers.RefreshSendUDPWorker
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.Horn
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
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

        while (true) {
            val receivedDipolData = receiver.receiveStringAndIPFromUDP()
            Log.d("UDP receiveLocalModeData", "Pair received: $receivedDipolData")

            receivedDipolData?.let {
//                Log.d("UDP receiveLocalModeData", "let")

//                val ar = it.split(" ")
                val ar = it.first.split(" ")
                if (ar[0] == "dipol") {
                    val id = ar[1].substring(0, ar[1].length - 1)
                    var already = 0


                    for (i in dipolListDto) {
                        if (i.id == id) {
// connected list control:
                            val myDipol = dipolsDao.getDipolItemById(id)
                            myDipol?.let { dipol ->
                                val timeString = System.currentTimeMillis() / 1000
                                dipolsDao.updateDipolItem(
                                    dipol.copy(
                                        connected = true,
                                        lastConnection = timeString
                                    )
                                )
                                Log.d(
                                    "dipolsDao.updateDipolItem",
                                    "${dipol.dipolId} ${dipol.connected} ${dipol.lastConnection}"
                                )
                            }

                            already = 1
                            break
                        }
                    }

                    if (already == 0) {

                        val dipolDto = DipolDto(
                            id,
                            it.second,
                            it.first
                        )
                        dipolListDto.add(dipolDto)
                        Log.d("UDP receiveLocalModeData", "dipol $dipolDto added")
                        Log.d("UDP receiveLocalModeData", "dipolListDto $dipolListDto")

                        val itemToAdd = mapper.mapDtoToDbModel(dipolDto)
                        Log.d("UDP receiveLocalModeData", "itemToAdd = $itemToAdd")
                        val itemFromDb = dipolsDao.getDipolItemById(dipolDto.id)
                        Log.d("UDP receiveLocalModeData", "itemFromDb = $itemFromDb")
                        if (itemFromDb == null) {
                            dipolsDao.addDipolItem(itemToAdd)
                        } else {
                            val itemToAddFromDb = itemFromDb.copy(connected = true)
                            Log.d("UDP receiveLocalModeData", "itemToAddFromDb = $itemToAddFromDb")
                            dipolsDao.addDipolItem(itemToAddFromDb)
                        }

                    }
                } else if (ar[0] == "5lights") {

                    val id = ar[1].substring(0, ar[1].length - 1)
                    var already = 0


//                    for (i in dipolListDto) {
                    if (fiveLights.id == id) {
// connected list control:
                        val myFiveLights = dipolsDao.getFiveLightsItemById(id)
                        myFiveLights?.let { fiveLights ->
                            val timeString = System.currentTimeMillis() / 1000
                            dipolsDao.updateFiveLightsItem(
                                fiveLights.copy(
                                    connected = true,
                                    lastConnection = timeString
                                )
                            )
                            Log.d(
                                "dipolsDao.updateFiveLightsItem",
                                "${fiveLights.fiveLightsId} ${fiveLights.connected} ${fiveLights.lastConnection}"
                            )
                        }

                        already = 1
//                        break
                    }
//                    }

                    if (already == 0) {

                        fiveLights = DipolDto(
                            id,
                            it.second,
                            it.first
                        )
//                        dipolListDto.add(dipolDto)
                        Log.d("UDP receiveLocalModeData", "dipol $fiveLights added")
//                        Log.d("UDP receiveLocalModeData", "dipolListDto $dipolListDto")

                        val itemToAdd = mapper.mapFiveLightsDtoToDbModel(fiveLights)
                        Log.d("UDP receiveLocalModeData", "itemToAdd = $itemToAdd")
                        val itemFromDb = dipolsDao.getFiveLightsItemById(fiveLights.id)
                        Log.d("UDP receiveLocalModeData", "itemFromDb = $itemFromDb")
                        if (itemFromDb == null) {
                            dipolsDao.addFiveLightsItem(itemToAdd)
                        } else {
                            val itemToAddFromDb = itemFromDb.copy(connected = true)
                            Log.d("UDP receiveLocalModeData", "itemToAddFromDb = $itemToAddFromDb")
                            dipolsDao.addFiveLightsItem(itemToAddFromDb)
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
        return Transformations.map(dipolsDao.getFiveLightsItemLD()) { it ->
            it?.let {
                mapper.mapFiveLightsDbModelToEntity(it)
            }
        }
    }

    override fun unselectDipol() {
        val selectedDipol = dipolsDao.getSelectedDipolItem(true)
        selectedDipol?.let { dipolsDao.updateDipolItem(it.copy(selected = false)) }
    }


    override fun workerStartStop() {
        val workManager = WorkManager.getInstance(application)
        val infoLF = workManager.getWorkInfosForUniqueWork(RefreshSendUDPWorker.WORK_NAME)

        val workerState = infoLF.get()[0].state.toString()
        Log.d("onClick workerStartStop", "state = $workerState")
        if (workerState == "RUNNING") {
            Log.d("onClick workerStartStop", "workerState == \"RUNNING\"")
            workManager.cancelAllWork()
        } else {
            Log.d("onClick workerStartStop", "workerState == \"CANCELED\"")
            workManager.enqueueUniqueWork(
                RefreshSendUDPWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,  //what to do, if another worker will be started
                RefreshSendUDPWorker.makeRequest()
            )
        }

    }

    override fun getIsBroadcast(): LiveData<Boolean> {
        val workManager = WorkManager.getInstance(application)
        val infoLD = workManager.getWorkInfosForUniqueWorkLiveData(RefreshSendUDPWorker.WORK_NAME)
        return Transformations.map(infoLD) {
            it[0].state.toString() == "RUNNING"
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
        Log.d("getDipolList", "were here")

        return Transformations.map(dipolsDao.getConnectedDipolListLD()) { it ->
            Log.d("getDipolList", "$it")
            it.map {
                mapper.mapDbModelToEntity(it)
            }
        }
    }

    override fun selectDipolItem(dipolId: String) {
        Log.d("onClickListener", " SelectedItem: $dipolId")

        if(dipolId == "b4e62d52abc2"){
            val oldSelectedItem = dipolsDao.getSelectedDipolItem(true)
            oldSelectedItem?.let {
                dipolsDao.updateDipolItem(it.copy(selected = false))
            }
            val fiveLightsDbModel = dipolsDao.getFiveLightsItemById(dipolId)
            fiveLightsDbModel?.let { dipolsDao.updateFiveLightsItem(fiveLightsDbModel.copy(selected = true)) }
        } else {
            val oldSelectedItem = dipolsDao.getSelectedDipolItem(true)
            Log.d("onDipolItemClickListener", " oldSelectedItem: ${oldSelectedItem?.dipolId}")
            val fiveLightsDbModel = dipolsDao.getFiveLightsItemById("b4e62d52abc2")
            Log.d("onDipolItemClickListener", " fiveSelectedItem: ${fiveLightsDbModel?.fiveLightsId} ${fiveLightsDbModel?.selected}")

            if (oldSelectedItem == null && fiveLightsDbModel?.selected == true) {
                dipolsDao.updateFiveLightsItem(fiveLightsDbModel.copy(selected = false))
                val newSelectedItem = dipolsDao.getDipolItemById(dipolId)
                newSelectedItem?.let {
                    dipolsDao.updateDipolItem(it.copy(selected = true))
                }
            }else{

                val newSelectedItem = dipolsDao.getDipolItemById(dipolId)
//                Log.d("onDipolItemClickListener", " newSelectedItem: ${newSelectedItem?.dipolId}")

                if (oldSelectedItem?.dipolId != newSelectedItem?.dipolId) {

                    oldSelectedItem?.let {
                        dipolsDao.updateDipolItem(it.copy(selected = false))
                    }
                    newSelectedItem?.let {
                        dipolsDao.updateDipolItem(it.copy(selected = true))
                    }
                }
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
        if (set == "dipol"){
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
        } else if (set == "fiveLights"){
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
                Log.d("DipoliaRepositoryImpl", "changeLocalState newFiveLightsItem $newFiveLightsItem")
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