package com.example.dipolia.data.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.domain.useCases.GetConnectedLampsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

class SendColorListWorker (
    context: Context,
    workerParameters: WorkerParameters,
    private val sender: UDPClient,
    private val getLampsUseCase: GetConnectedLampsUseCase
) : CoroutineWorker(context, workerParameters) {


    override suspend fun doWork(): Result {

        var rabbitColorSpeed = 0.5

        while (true) {
            Log.d("SendColorListWorker", "while (true)")

            getLampsUseCase().collectLatest { lamps ->
                Log.d("SendColorListWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")

                for (lamp in lamps ) {
                    val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN))
                    var stringToSend = ""

                    if (lamp.lampType == LampType.DIPOL) {

                        val r1 = (BigDecimal(lamp.c.colors[0]).setScale(3, RoundingMode.HALF_DOWN))
                        val g1 = (BigDecimal(lamp.c.colors[1]).setScale(3, RoundingMode.HALF_DOWN))
                        val b1 = (BigDecimal(lamp.c.colors[2]).setScale(3, RoundingMode.HALF_DOWN))
                        val r2 = (BigDecimal(lamp.c.colors[3]).setScale(3, RoundingMode.HALF_DOWN))
                        val g2 = (BigDecimal(lamp.c.colors[4]).setScale(3, RoundingMode.HALF_DOWN))
                        val b2 = (BigDecimal(lamp.c.colors[5]).setScale(3, RoundingMode.HALF_DOWN))

                        stringToSend = "r1=$r1;g1=$g1;b1=$b1;r2=$r2;g2=$g2;b2=$b2;rcs=$rcs"

                    } else if (lamp.lampType == LampType.FIVE_LIGHTS) {

                        val r = (BigDecimal(lamp.c.colors[0]).setScale(3, RoundingMode.HALF_DOWN))
                        val g = (BigDecimal(lamp.c.colors[1]).setScale(3, RoundingMode.HALF_DOWN))
                        val b = (BigDecimal(lamp.c.colors[2]).setScale(3, RoundingMode.HALF_DOWN))
                        val w = (BigDecimal(lamp.c.colors[3]).setScale(3, RoundingMode.HALF_DOWN))
                        val u = (BigDecimal(lamp.c.colors[4]).setScale(3, RoundingMode.HALF_DOWN))

                        stringToSend = "r=$r;g=$g;b=$b;w=$w;u=$u;rcs=$rcs"
                    }
                    val address = sender.getInetAddressByName(lamp.ip)
//                Log.d("sendColors", "$stringToSend, $address")
                    sender.sendUDPSuspend(stringToSend, address)
                }
                delay(100)
            }
        }
    }


    companion object {

        private const val IP = "ip"
        private const val LAMPTYPE = "lamptype"
        private const val LIST = "list"

        const val WORK_NAME = "SendColorListWorker"

        fun makeRequest(): OneTimeWorkRequest {
//            Log.d("SendColorListWorker", "makeOneTimeRequest")
            return OneTimeWorkRequestBuilder<SendColorListWorker>()
                .build()
        }

        fun makePeriodicRequest(): PeriodicWorkRequest {
//            Log.d("SendColorListWorker", "makePeriodicRequest")
            return PeriodicWorkRequestBuilder<SendColorListWorker>(8, TimeUnit.MINUTES)
                .build()
        }
    }
}