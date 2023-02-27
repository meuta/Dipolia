package com.example.dipolia.data.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.network.UDPClient
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

            val dipolList = dipolsDao.getDipolList()
            for (dipol in dipolList){
                if (dipol.connected) {
                    Log.d("worker", "IP = ${dipol.dipolIp}")
                    val rcs = (BigDecimal(rabbitColorSpeed).setScale(
                        3,
                        RoundingMode.HALF_DOWN
                    ))

                    val r1 = (BigDecimal(dipol.r1).setScale(3, RoundingMode.HALF_DOWN))
                    val g1 = (BigDecimal(dipol.g1).setScale(3, RoundingMode.HALF_DOWN))
                    val b1 = (BigDecimal(dipol.b1).setScale(3, RoundingMode.HALF_DOWN))
                    val r2 = (BigDecimal(dipol.r2).setScale(3, RoundingMode.HALF_DOWN))
                    val g2 = (BigDecimal(dipol.g2).setScale(3, RoundingMode.HALF_DOWN))
                    val b2 = (BigDecimal(dipol.b2).setScale(3, RoundingMode.HALF_DOWN))

                    val stringToSend: String = "r1=$r1;g1=$g1;b1=$b1;r2=$r2;g2=$g2;b2=$b2;rcs=$rcs"

                    val address = sender.getInetAddressByName(dipol.dipolIp)

                    sender.sendUDPSuspend(stringToSend, address)
                }
            }
            val fiveLigths = dipolsDao.getFiveLightsItemById("b4e62d52abc2")
            fiveLigths?.let {
                if (fiveLigths.connected) {
                    Log.d("worker", "IP = ${fiveLigths.fiveLightsIp}")
                    val rcs = (BigDecimal(rabbitColorSpeed).setScale(
                        3,
                        RoundingMode.HALF_DOWN
                    ))

                    val r = (BigDecimal(fiveLigths.r).setScale(3, RoundingMode.HALF_DOWN))
                    val g = (BigDecimal(fiveLigths.g).setScale(3, RoundingMode.HALF_DOWN))
                    val b = (BigDecimal(fiveLigths.b).setScale(3, RoundingMode.HALF_DOWN))
                    val w = (BigDecimal(fiveLigths.w).setScale(3, RoundingMode.HALF_DOWN))
                    val u = (BigDecimal(fiveLigths.uv).setScale(3, RoundingMode.HALF_DOWN))


                    val stringToSend: String = "r=$r;g=$g;b=$b;w=$w;u=$u;rcs=$rcs"

                    val address = sender.getInetAddressByName(fiveLigths.fiveLightsIp)

                    sender.sendUDPSuspend(stringToSend, address)
                }
            }

            delay(100)
        }
    }


    companion object {

        const val WORK_NAME = "RefreshSendUDPWorker"

        fun makeRequest(): OneTimeWorkRequest {
            Log.d("RefreshSendUDPWorker", "makeOneTimeRequest")
            return OneTimeWorkRequestBuilder<RefreshSendUDPWorker>().build()
        }

        fun makePeriodicRequest(): PeriodicWorkRequest {
            Log.d("RefreshSendUDPWorker", "makePeriodicRequest")
            return PeriodicWorkRequestBuilder<RefreshSendUDPWorker>(8, TimeUnit.MINUTES)
                .build()
        }
    }
}