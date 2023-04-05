package com.example.dipolia.data.workers

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import com.example.dipolia.data.LampsRepositoryImpl
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

class SendColorListWorker(
    context: Context,
    private val workerParameters: WorkerParameters,
    private val sender: UDPClient
) : CoroutineWorker(context, workerParameters) {


    //    private val application = context as Application
//    private val repository = LampsRepositoryImpl(application)
//    private val myData = workerParameters.inputData
    override suspend fun doWork(): Result {
//        Log.d("SendColorListWorker", "application = $application")
//        Log.d("workerParameters", "data $myData")

        val ip = workerParameters.inputData.getString(IP) ?: ""
        val lampType = workerParameters.inputData.getString(LAMPTYPE) ?: ""
        val array = workerParameters.inputData.getDoubleArray(LIST) ?:doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        Log.d("SendColorListWorker", "lampType $lampType")


        var rabbitColorSpeed = 0.5

        while (true) {
            Log.d("SendColorListWorker", "while (true)")

//            val lampList = repository.lampEntityList
//            val selectedLamp = repository.selectedLamp
//            Log.d("SendColorListWorker", "selectedLamp = $selectedLamp")
//
//                for (lamp in lampList) {
////                if (lamp.connected) {
//                    Log.d("worker", "IP = ${lamp.ip}")
            val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN))
            var stringToSend = ""
//                    if (lamp.lampType == LampType.DIPOl) {
                    if (lampType == "dipol") {
            val r1 = (BigDecimal(array[0]).setScale(3, RoundingMode.HALF_DOWN))
            val g1 = (BigDecimal(array[1]).setScale(3, RoundingMode.HALF_DOWN))
            val b1 = (BigDecimal(array[2]).setScale(3, RoundingMode.HALF_DOWN))
            val r2 = (BigDecimal(array[3]).setScale(3, RoundingMode.HALF_DOWN))
            val g2 = (BigDecimal(array[4]).setScale(3, RoundingMode.HALF_DOWN))
            val b2 = (BigDecimal(array[5]).setScale(3, RoundingMode.HALF_DOWN))
//
            stringToSend = "r1=$r1;g1=$g1;b1=$b1;r2=$r2;g2=$g2;b2=$b2;rcs=$rcs"
//
//                    } else if (lamp.lampType == LampType.FIVE_LIGHTS){
                    } else if (lampType == "fiveLights"){
                        val r = (BigDecimal(array[0]).setScale(3, RoundingMode.HALF_DOWN))
                        val g = (BigDecimal(array[1]).setScale(3, RoundingMode.HALF_DOWN))
                        val b = (BigDecimal(array[2]).setScale(3, RoundingMode.HALF_DOWN))
                        val w = (BigDecimal(array[3]).setScale(3, RoundingMode.HALF_DOWN))
                        val u = (BigDecimal(array[4]).setScale(3, RoundingMode.HALF_DOWN))

                        stringToSend = "r=$r;g=$g;b=$b;w=$w;u=$u;rcs=$rcs"
                    }
//
//                    val address = sender.getInetAddressByName(lamp.ip)
            val address = sender.getInetAddressByName(ip)
            sender.sendUDPSuspend(stringToSend, address)
//                }
////            }

//            repository.getLatestLampList().collect { lampList ->
//                Log.d("SendColorListWorker", "lampList = ${lampList.map { it.id }}")
//
//                for (lamp in lampList) {
////                if (lamp.connected) {
//                    Log.d("worker", "IP = ${lamp.ip}")
//                    val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN))
//                    var stringToSend = ""
//                    if (lamp.lampType == LampType.DIPOl) {
//                        val r1 = (BigDecimal(lamp.c.colors[0]).setScale(3, RoundingMode.HALF_DOWN))
//                        val g1 = (BigDecimal(lamp.c.colors[1]).setScale(3, RoundingMode.HALF_DOWN))
//                        val b1 = (BigDecimal(lamp.c.colors[2]).setScale(3, RoundingMode.HALF_DOWN))
//                        val r2 = (BigDecimal(lamp.c.colors[3]).setScale(3, RoundingMode.HALF_DOWN))
//                        val g2 = (BigDecimal(lamp.c.colors[4]).setScale(3, RoundingMode.HALF_DOWN))
//                        val b2 = (BigDecimal(lamp.c.colors[5]).setScale(3, RoundingMode.HALF_DOWN))
//
//                        stringToSend = "r1=$r1;g1=$g1;b1=$b1;r2=$r2;g2=$g2;b2=$b2;rcs=$rcs"
//
//                    } else if (lamp.lampType == LampType.FIVE_LIGHTS){
//                        val r = (BigDecimal(lamp.c.colors[0]).setScale(3, RoundingMode.HALF_DOWN))
//                        val g = (BigDecimal(lamp.c.colors[1]).setScale(3, RoundingMode.HALF_DOWN))
//                        val b = (BigDecimal(lamp.c.colors[2]).setScale(3, RoundingMode.HALF_DOWN))
//                        val w = (BigDecimal(lamp.c.colors[3]).setScale(3, RoundingMode.HALF_DOWN))
//                        val u = (BigDecimal(lamp.c.colors[4]).setScale(3, RoundingMode.HALF_DOWN))
//
//                        stringToSend = "r=$r;g=$g;b=$b;w=$w;u=$u;rcs=$rcs"
//                    }
//
//                    val address = sender.getInetAddressByName(lamp.ip)
//                    sender.sendUDPSuspend(stringToSend, address)
//                }
////            }
//
//            }
            delay(1000)
        }


    }


    companion object {

        private const val IP = "ip"
        private const val LAMPTYPE = "lamptype"
        private const val LIST = "list"

        const val WORK_NAME = "SendColorListWorker"

        //        fun makeRequest(myLamps: LiveData<List<LampDomainEntity>>): OneTimeWorkRequest {
//        fun makeRequest(myLamps: List<LampDomainEntity>): OneTimeWorkRequest {
        fun makeRequest(ip: String, lampType: String, colorList: List<Double>): OneTimeWorkRequest {
//        fun makeRequest(): OneTimeWorkRequest {
//            Log.d("SendColorListWorker", "makeOneTimeRequest")
            val array = colorList.toDoubleArray()
            return OneTimeWorkRequestBuilder<SendColorListWorker>()
                .setInputData(
                    workDataOf(
                        IP to ip,
                        LAMPTYPE to lampType,
                        LIST to array
                    )
                )     //Kotlin method for Pair creating
                .build()
        }

        fun makePeriodicRequest(): PeriodicWorkRequest {
//            Log.d("SendColorListWorker", "makePeriodicRequest")
            return PeriodicWorkRequestBuilder<SendColorListWorker>(8, TimeUnit.MINUTES)
                .build()
        }
    }
}