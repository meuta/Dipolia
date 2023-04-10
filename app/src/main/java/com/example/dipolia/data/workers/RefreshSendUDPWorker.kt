package com.example.dipolia.data.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.domain.entities.LampType
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

class RefreshSendUDPWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private val dipolsDao = AppDatabase.getInstance(context).dipolsDao()
    private val sender = UDPClient()

    override suspend fun doWork(): Result {

        var rabbitColorSpeed = 0.5

        while (true) {

            val lampList = dipolsDao.getLampsList()
            for (lamp in lampList) {
//                if (lamp.connected) {
//                    Log.d("worker", "IP = ${lamp.lampId}")
                    val rcs = (BigDecimal(rabbitColorSpeed).setScale(
                        3,
                        RoundingMode.HALF_DOWN
                    ))
                    var stringToSend = ""
                    if (lamp.lampType == LampType.DIPOL) {
                        val r1 = (BigDecimal(lamp.colorList.colors[0]).setScale(3, RoundingMode.HALF_DOWN))
                        val g1 = (BigDecimal(lamp.colorList.colors[1]).setScale(3, RoundingMode.HALF_DOWN))
                        val b1 = (BigDecimal(lamp.colorList.colors[2]).setScale(3, RoundingMode.HALF_DOWN))
                        val r2 = (BigDecimal(lamp.colorList.colors[3]).setScale(3, RoundingMode.HALF_DOWN))
                        val g2 = (BigDecimal(lamp.colorList.colors[4]).setScale(3, RoundingMode.HALF_DOWN))
                        val b2 = (BigDecimal(lamp.colorList.colors[5]).setScale(3, RoundingMode.HALF_DOWN))

                        stringToSend = "r1=$r1;g1=$g1;b1=$b1;r2=$r2;g2=$g2;b2=$b2;rcs=$rcs"

                    } else if (lamp.lampType == LampType.FIVE_LIGHTS){
                        val r = (BigDecimal(lamp.colorList.colors[0]).setScale(3, RoundingMode.HALF_DOWN))
                        val g = (BigDecimal(lamp.colorList.colors[1]).setScale(3, RoundingMode.HALF_DOWN))
                        val b = (BigDecimal(lamp.colorList.colors[2]).setScale(3, RoundingMode.HALF_DOWN))
                        val w = (BigDecimal(lamp.colorList.colors[3]).setScale(3, RoundingMode.HALF_DOWN))
                        val u = (BigDecimal(lamp.colorList.colors[4]).setScale(3, RoundingMode.HALF_DOWN))

                        stringToSend = "r=$r;g=$g;b=$b;w=$w;u=$u;rcs=$rcs"
                    }

//                    val address = sender.getInetAddressByName(lamp.lampIp)
//                    sender.sendUDPSuspend(stringToSend, address)
                }
//            }

            delay(100)
        }
    }


    companion object {

        const val WORK_NAME = "RefreshSendUDPWorker"

        fun makeRequest(): OneTimeWorkRequest {
//            Log.d("RefreshSendUDPWorker", "makeOneTimeRequest")
            return OneTimeWorkRequestBuilder<RefreshSendUDPWorker>().build()
        }

        fun makePeriodicRequest(): PeriodicWorkRequest {
//            Log.d("RefreshSendUDPWorker", "makePeriodicRequest")
            return PeriodicWorkRequestBuilder<RefreshSendUDPWorker>(8, TimeUnit.MINUTES)
                .build()
        }
    }
}