package com.example.dipolia.data.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.database.DipolDbModel
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.network.UDPClient
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class RefreshSendUDPWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private val dipolsDao = AppDatabase.getInstance(context).dipolsDao()
    private val mapper = DipoliaMapper()
    private val sender = UDPClient()

    override suspend fun doWork(): Result {
        val r1 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toString()
//        val r1 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN))
        val g1 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toString()
        val b1 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toString()
        val r2 = (BigDecimal(0.5).setScale(3, RoundingMode.HALF_DOWN)).toString()
        val g2 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toString()
        val b2 = (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toString()
        var rabbitColorSpeed = 0.5

        val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN)).toString()

        val s1: String = "r1=" + r2 + ";g1=" + r2 + ";b1=" + b1 +
                ";r2=" + r2 + ";g2=" + g2 + ";b2=" + r2 + ";rcs=" + rcs

        val s2: String = "r1=" + r1 + ";g1=" + r2 + ";b1=" + r2 +
                ";r2=" + r2 + ";g2=" + r2 + ";b2=" + b2 + ";rcs=" + rcs

        val s3: String = "r1=" + r2 + ";g1=" + g1 + ";b1=" + r2 +
                ";r2=" + r1 + ";g2=" + r2 + ";b2=" + r2 + ";rcs=" + rcs

        val s4: String = "r1=" + r2 + ";g1=" + g1 + ";b1=" + b1 +
                ";r2=" + r1 + ";g2=" + r2 + ";b2=" + b2 + ";rcs=" + rcs

        val s5: String = "r1=" + r1 + ";g1=" + r2 + ";b1=" + b1 +
                ";r2=" + r1 + ";g2=" + g2 + ";b2=" + r2 + ";rcs=" + rcs

        val s6: String = "r1=" + r1 + ";g1=" + g1 + ";b1=" + r2 +
                ";r2=" + r2 + ";g2=" + g2 + ";b2=" + b2 + ";rcs=" + rcs

        val list = arrayListOf<String>(s1, s2, s3, s4, s5, s6)
        while (true) {
            sender.sendUDPSuspend(list.random(), sender.getInetAddressByName("192.168.0.150"))

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

            sender.sendUDPSuspend(list.random(), sender.getInetAddressByName("192.168.0.133"))
//            sender.sendUDPSuspend(list.random(), sender.getInetAddressByName("192.168.0.127"))
//            delay(290000)
            delay(2563)
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