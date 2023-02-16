package com.example.dipolia.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.database.DipolDbModel
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.network.DipolDto
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.data.network.UDPServer
import com.example.dipolia.data.workers.RefreshSendUDPWorker
import com.example.dipolia.domain.ColorComponent
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
                Log.d("UDP receiveLocalModeData", "let")

//                val ar = it.split(" ")
                val ar = it.first.split(" ")
                if (ar[0] == "dipol") {
                    val id = ar[1].substring(0, ar[1].length - 1)
                    var already = 0


                    for (i in dipolListDto) {
                        if (i.id == id) {
// connected list control:
                            dipolsDao.updateDipolItem(mapper.mapDtoToDbModel(i).copy(connected = true))

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
                        dipolsDao.addDipolItem(mapper.mapDtoToDbModel(dipolDto))
                    }
                }
            }
        }
    }

    override suspend fun refreshConnectedList() {
        val notConnectedList = dipolsDao.getDipolList()
        val refreshedList = notConnectedList
            .filter { it.connected }
            .map{ it.copy(connected = false) }
        for (dipol in refreshedList) {
            dipolsDao.updateDipolItem(dipol)
        }
    }


    //    override suspend fun testSendLocalModeData(dipolID: String, string: String) {
    override fun testSendLocalModeData(dipolID: String, string: String) {

////        val r1 = (BigDecimal(i.c1.r).setScale(3, RoundingMode.HALF_DOWN)).toString()
////        val g1 = (BigDecimal(i.c1.g).setScale(3, RoundingMode.HALF_DOWN)).toString()
////        val b1 = (BigDecimal(i.c1.b).setScale(3, RoundingMode.HALF_DOWN)).toString()
////        val r2 = (BigDecimal(i.c2.r).setScale(3, RoundingMode.HALF_DOWN)).toString()
////        val g2 = (BigDecimal(i.c2.g).setScale(3, RoundingMode.HALF_DOWN)).toString()
////        val b2 = (BigDecimal(i.c2.b).setScale(3, RoundingMode.HALF_DOWN)).toString()
////
////        val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN)).toString()
//
//        val r1 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toString()
////        val r1 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN))
//        val g1 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toString()
//        val b1 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toString()
//        val r2 = (BigDecimal(0.5).setScale(3, RoundingMode.HALF_DOWN)).toString()
//        val g2 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toString()
//        val b2 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toString()
//        var rabbitColorSpeed = 0.5
//
//        val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN)).toString()
//
//        val s1: String = "r1=" + r1 + ";g1=" + g1 + ";b1=" + b1 +
//                ";r2=" + r2 + ";g2=" + g2 + ";b2=" + r2 + ";rcs=" + rcs
//
//        val s2: String = "r1=" + r1 + ";g1=" + r2 + ";b1=" + b1 +
//                ";r2=" + r1 + ";g2=" + g2 + ";b2=" + b2 + ";rcs=" + rcs
//
//        val s3: String = "r1=" + g1 + ";g1=" + r1 + ";b1=" + b1 +
//                ";r2=" + r1 + ";g2=" + r2 + ";b2=" + r2 + ";rcs=" + rcs
//
//        val list = arrayListOf<String>(s1, s2, s3)
////        val s4 = list.random()
//        while (true) {
////            sender.sendUDPSuspend(list.random(), InetAddress.getByName("192.168.0.150"))
//            sender.sendUDPSuspend(list.random(), sender.getInetAddressByName("192.168.0.150"))
//
//            dipolsDao.addDipolItem(
//                DipolDbModel(
//                    dipolID,
//                    "/192.168.0.150",
//                    r1.toDouble(),
//                    g1.toDouble(),
//                    b1.toDouble(),
//                    r2.toDouble(),
//                    g2.toDouble(),
//                    b2.toDouble()
//                )
//            )
//
//            sender.sendUDPSuspend(list.random(), sender.getInetAddressByName("192.168.0.133"))
//            sender.sendUDPSuspend(list.random(), sender.getInetAddressByName("192.168.0.127"))
//            delay(290000)
//        }

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

        var oldSelectedItem = dipolsDao.getSelectedDipolItem(true)
        Log.d("selectDipolItem", " oldSelectedItem: ${oldSelectedItem?.dipolId}")

        var newSelectedItem = dipolsDao.getDipolItemById(dipolId)
        Log.d("selectDipolItem", " newSelectedItem: ${newSelectedItem.dipolId}")

        if (oldSelectedItem?.dipolId != newSelectedItem.dipolId) {
            oldSelectedItem = oldSelectedItem?.copy(selected = false)
            oldSelectedItem?.let {
                dipolsDao.updateDipolItem(it)
            }
            newSelectedItem = newSelectedItem.copy(selected = true)
            dipolsDao.updateDipolItem(newSelectedItem)
        }
    }

    override fun changeLocalState(
        dipolItem: DipolDomainEntity,
        horn: Horn,
        component: ColorComponent,
        componentDiff: Double
    ) {
        TODO("Not yet implemented")
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