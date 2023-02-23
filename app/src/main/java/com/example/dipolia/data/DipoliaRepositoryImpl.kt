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
            delay(1000)
        }
    }

    override suspend fun receiveLocalModeData() {

        val dipolListDto = mutableListOf<DipolDto>()

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
                                dipolsDao.updateDipolItem(dipol.copy(connected = true))
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
                        if (itemFromDb == null){
                            dipolsDao.addDipolItem(itemToAdd)
                        } else{
                            val itemToAddFromDb = itemFromDb.copy(connected = true)
                            Log.d("UDP receiveLocalModeData", "itemToAddFromDb = $itemToAddFromDb")
                            dipolsDao.addDipolItem(itemToAddFromDb)
                        }

                    }
                }
            }
        }
    }

    override suspend fun refreshConnectedList() {
//        while (true) {
//            val notConnectedList = dipolsDao.getDipolList()
//            val refreshedList = notConnectedList
//                .filter { it.connected }
//                .map { it.copy(connected = false) }
//            for (dipol in refreshedList) {
//                dipolsDao.updateDipolItem(dipol)
//            }
//            delay(60000)
//        }
        val notConnectedList = dipolsDao.getDipolList()
            val refreshedList = notConnectedList
                .filter { it.connected }
                .map { it.copy(connected = false) }
            for (dipol in refreshedList) {
                dipolsDao.updateDipolItem(dipol)
            }
    }

    override fun getSelectedDipol(): LiveData<DipolDomainEntity?> {
        return Transformations.map(dipolsDao.getSelectedDipolItemLD(true)) { it ->
//            it ?: DipolDbModel("", "", false, false)
            it?.let {
                mapper.mapDbModelToEntity(it)
            }
        }
//        return dipolsDao.getSelectedDipolItemLD(true)
    }

    override fun unselectDipol() {
        val selectedDipol = dipolsDao.getSelectedDipolItem(true)
        selectedDipol?.let { dipolsDao.updateDipolItem(it.copy(selected = false)) }
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


        override fun getDipolList(): LiveData<List<DipolDomainEntity>> {
        Log.d("getDipolList", "were here")

        return Transformations.map(dipolsDao.getDipolListLD(true)) { it ->
            Log.d("getDipolList", "$it")
//            it
//            mapper.mapListDbModelToEntity(it)
            it.map{
                mapper.mapDbModelToEntity(it)
//                mapper.mapDbModelToEntity(it.copy(connected = false))
            }

        }
    }

    override fun selectDipolItem(dipolId: String) {

        val oldSelectedItem = dipolsDao.getSelectedDipolItem(true)
        Log.d("onDipolItemClickListener", " oldSelectedItem: ${oldSelectedItem?.dipolId}")

        val newSelectedItem = dipolsDao.getDipolItemById(dipolId)
        Log.d("onDipolItemClickListener", " newSelectedItem: ${newSelectedItem?.dipolId}")

        if (oldSelectedItem?.dipolId != newSelectedItem?.dipolId) {

//            Log.d("onDipolItemClickListener", " oldSelectedItemCopied: ${oldSelectedItem?.dipolId}")

            oldSelectedItem?.let {
                dipolsDao.updateDipolItem(it.copy(selected = false))
            }
            newSelectedItem?.let {
//                val newSelected = it.copy(selected = true)
//                Log.d(
//                    "onDipolItemClickListener",
//                    " newSelectedItemCopied: ${newSelected.dipolId}"
//                )
                dipolsDao.updateDipolItem(it.copy(selected = true))
            }
        }
    }


//    override fun changeLocalState(
//        dipolItem: DipolDomainEntity,
//        horn: Horn,
//        component: ColorComponent,
//        componentDiff: Double
//    ) {
    override fun changeLocalState(index: Int, value: Double){
    Log.d("DipoliaRepositoryImpl", "changeLocalState $index $value")

    val oldDipolItem = dipolsDao.getSelectedDipolItem(true)
        Log.d("DipoliaRepositoryImpl", "changeLocalState $oldDipolItem")

        oldDipolItem?.let {
            val newDipolItem = when (index){
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