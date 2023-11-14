package com.example.dipolia.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.domain.useCases.GetConnectedLampsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.RoundingMode

@HiltWorker
class SendLoopColorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val sender: UDPClient,
    private val getLampsUseCase: GetConnectedLampsUseCase
) : CoroutineWorker(context, workerParameters) {


    override suspend fun doWork(): Result {
        val isLooping = inputData.getBoolean(KEY_IS_LOOPING_VALUE,false)
        var rabbitColorSpeed = 0.5

        var varianceR = 0.0
        var varianceG = 0.0
        var varianceB = 1.0
        var count = 0
        while (true) {
            Log.d("SendLoopColorWorker", "while (true)")

//            getLampsUseCase().collectLatest { lamps ->
            getLampsUseCase().collect { lamps ->
//                Log.d("SendColorListWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")
                Log.d("WO isLooping = ", "$isLooping")
                count += 1
                count %= 301
                Log.d("SendLoopColorWorker variance", "count    =  $count")

                if (count <= 100) {varianceR += 0.01} else {varianceR -= 0.01}
                varianceR = when (count) {
                        in 1 .. 100 -> (BigDecimal(varianceR + 0.01).setScale(3, RoundingMode.HALF_DOWN)).toDouble()
                        in 101 .. 200 -> (BigDecimal(varianceR - 0.01).setScale(3, RoundingMode.HALF_DOWN)).toDouble()
                        else -> (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toDouble()
                }
                varianceG = when (count) {
                        in 1 .. 100 -> (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toDouble()
                        in 101 .. 200 -> (BigDecimal(varianceG + 0.01).setScale(3, RoundingMode.HALF_DOWN)).toDouble()
                        else -> (BigDecimal(varianceG - 0.01).setScale(3, RoundingMode.HALF_DOWN)).toDouble()
                }
                varianceB = when (count) {
                        in 1 .. 100 -> (BigDecimal(varianceB - 0.01).setScale(3, RoundingMode.HALF_DOWN)).toDouble()
                        in 101 .. 200 -> (BigDecimal(0.0).setScale(3, RoundingMode.HALF_DOWN)).toDouble()
                        else -> (BigDecimal(varianceB + 0.01).setScale(3, RoundingMode.HALF_DOWN)).toDouble()
                }

                Log.d("SendLoopColorWorker variance", "varianceR=  $varianceR")
                Log.d("SendLoopColorWorker variance", "varianceG=  $varianceG")
                Log.d("SendLoopColorWorker variance", "varianceB=  $varianceB")

                for (lamp in lamps ) {
                    Log.d("SendLoopColorWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")

                    Log.d("SendLoopColorWorker", "Lamp = ${lamp.id to lamp.c }")


                        val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN))
                        var stringToSend = ""

                        if (lamp.lampType == LampType.DIPOL) {

                            val r1 = varianceR
                            val g1 = varianceG
                            val b1 = varianceB
                            val r2 = varianceR
                            val g2 = varianceG
                            val b2 = varianceB

                            stringToSend = "r1=$r1;g1=$g1;b1=$b1;r2=$r2;g2=$g2;b2=$b2;rcs=$rcs"

                        } else if (lamp.lampType == LampType.FIVE_LIGHTS) {

//                            val r = (BigDecimal(lamp.c.colors[0]).setScale(3, RoundingMode.HALF_DOWN))
                            val r = varianceR
                            val g = varianceG
                            val b = varianceB
                            val w = (BigDecimal(lamp.c.colors[3]).setScale(3, RoundingMode.HALF_DOWN))
                            val u = (BigDecimal(lamp.c.colors[4]).setScale(3, RoundingMode.HALF_DOWN))

                            stringToSend = "r=$r;g=$g;b=$b;w=$w;u=$u;rcs=$rcs"
                        }
                        val address = sender.getInetAddressByName(lamp.ip)
                        Log.d("sendColors variance", "string   =  $stringToSend, $address")
                        sender.sendUDPSuspend(stringToSend, address)

                    delay(100)
                }
            }
        }
    }


    companion object {

        const val WORK_NAME = "SendLoopColorWorker"
        const val KEY_IS_LOOPING_VALUE = "key_isLooping"
        fun makeRequest(data: Data): OneTimeWorkRequest {
//            Log.d("SendColorListWorker", "makeOneTimeRequest")
            return OneTimeWorkRequestBuilder<SendLoopColorWorker>()
                .setInputData(data)
                .build()
        }

//        fun makePeriodicRequest(): PeriodicWorkRequest {
////            Log.d("SendColorListWorker", "makePeriodicRequest")
//            return PeriodicWorkRequestBuilder<SendColorListWorker>(15, TimeUnit.MINUTES)
//                .build()
//        }
    }
}