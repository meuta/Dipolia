package com.example.dipolia.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.domain.useCases.GetConnectedLampsUseCase
import com.example.dipolia.domain.useCases.GetStreamingStateUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@HiltWorker
class SendColorListWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val sender: UDPClient,
    private val getLampsUseCase: GetConnectedLampsUseCase,
    private val getStreamingStateUseCase: GetStreamingStateUseCase
) : CoroutineWorker(context, workerParameters) {

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun doWork(): Result {
        var isLooping = false
        var secondsChange = 0
        var secondsStay = 0

        scope.launch {
            getStreamingStateUseCase().collect {streamingState ->

                Log.d("getStreamingStateUseCase ", "isLooping = ${streamingState.isLooping}")
                Log.d("getStreamingStateUseCase ", "secondsChange = ${streamingState.secondsChange}")
                Log.d("getStreamingStateUseCase ", "secondsStay = ${streamingState.secondsStay}")
                streamingState.isLooping?.let { isLooping = it}
                streamingState.secondsChange?.let { secondsChange = (it*10).toInt()}
                streamingState.secondsStay?.let { secondsStay = (it*10).toInt()}
            }
        }

        var rabbitColorSpeed = 0.5

        var count = 0

        while (true) {
//            Log.d("SendColorListWorker", "while (true)")

            getLampsUseCase().collect { lamps ->
//                Log.d("SendColorListWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")
                Log.d("getLampsUseCase().collect ", "isLooping = $isLooping")
                Log.d("getLampsUseCase().collect ", "secondsChange = $secondsChange")
                Log.d("getLampsUseCase().collect ", "secondsStay = $secondsStay")

                if (isLooping && (secondsChange > 0)) {
                    count += 1
                    val period = (secondsChange + secondsStay) * 2
                    count %= period
                }

                for (lamp in lamps) {
//                    Log.d("SendColorListWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")
//                    Log.d("SendColorListWorker", "Lamp = ${lamp.id to lamp.c }")

                    if (lamp.c.colors.isNotEmpty()) {
                        val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN))
                        var stringToSend = ""

                        if (lamp.lampType == LampType.DIPOL) {
                            Log.d("SendColorListWorker", "Lamp = ${lamp.id to lamp.c}")

                            var r1Dif = 0.0
                            var r2Dif = 0.0
                            var r3Dif = 0.0

                            if (isLooping && (secondsChange > 0)) {
                                Log.d("isLooping && (paceChange > 0) ", "isLooping = $isLooping")
                                Log.d("isLooping && (paceChange > 0) ", "secondsChange = $secondsChange")
                                Log.d("isLooping && (paceChange > 0) ", "secondsStay = $secondsStay")

                                val factor = when (count) {
                                    in 1..secondsChange -> count
                                    in secondsChange + 1..secondsChange + secondsStay -> secondsChange
                                    in secondsChange + secondsStay + 1..(secondsChange * 2 + secondsStay) -> secondsChange * 2 + secondsStay + 1 - count
                                    in secondsChange * 2 + secondsStay + 1..(secondsChange + secondsStay) * 2 -> 0
                                    else -> 0
                                }
                                r1Dif = (lamp.c.colors[0] - lamp.c.colors[3]) / secondsChange * factor
                                r2Dif = (lamp.c.colors[1] - lamp.c.colors[4]) / secondsChange * factor
                                r3Dif = (lamp.c.colors[2] - lamp.c.colors[5]) / secondsChange * factor
                            }

                            val r1 = (BigDecimal(lamp.c.colors[0] - r1Dif).setScale(
                                3,
                                RoundingMode.HALF_DOWN
                            ))
                            val g1 = (BigDecimal(lamp.c.colors[1] - r2Dif).setScale(
                                3,
                                RoundingMode.HALF_DOWN
                            ))
                            val b1 = (BigDecimal(lamp.c.colors[2] - r3Dif).setScale(
                                3,
                                RoundingMode.HALF_DOWN
                            ))
                            val r2 = (BigDecimal(lamp.c.colors[3] + r1Dif).setScale(
                                3,
                                RoundingMode.HALF_DOWN
                            ))
                            val g2 = (BigDecimal(lamp.c.colors[4] + r2Dif).setScale(
                                3,
                                RoundingMode.HALF_DOWN
                            ))
                            val b2 = (BigDecimal(lamp.c.colors[5] + r3Dif).setScale(
                                3,
                                RoundingMode.HALF_DOWN
                            ))

                            stringToSend = "r1=$r1;g1=$g1;b1=$b1;r2=$r2;g2=$g2;b2=$b2;rcs=$rcs"
                            Log.d(
                                "SendColorListWorker",
                                "Lamp = (${lamp.id}, count = $count  string =  $stringToSend"
                            )

                        } else if (lamp.lampType == LampType.FIVE_LIGHTS) {

                            val r =
                                (BigDecimal(lamp.c.colors[0]).setScale(3, RoundingMode.HALF_DOWN))
                            val g =
                                (BigDecimal(lamp.c.colors[1]).setScale(3, RoundingMode.HALF_DOWN))
                            val b =
                                (BigDecimal(lamp.c.colors[2]).setScale(3, RoundingMode.HALF_DOWN))
                            val w =
                                (BigDecimal(lamp.c.colors[3]).setScale(3, RoundingMode.HALF_DOWN))
                            val u =
                                (BigDecimal(lamp.c.colors[4]).setScale(3, RoundingMode.HALF_DOWN))

                            stringToSend = "r=$r;g=$g;b=$b;w=$w;u=$u;rcs=$rcs"
                            Log.d("sendColors", "string   =  $stringToSend")
                        }
                        val address = sender.getInetAddressByName(lamp.ip)
                        sender.sendUDPSuspend(stringToSend, address)
                    }
//                    delay(100)
                }
                delay(100)

            }
        }

    }


    companion object {

        const val WORK_NAME = "SendColorListWorker"

        fun makeRequest(): OneTimeWorkRequest {
//            Log.d("SendColorListWorker", "makeOneTimeRequest")
            return OneTimeWorkRequestBuilder<SendColorListWorker>()
                .build()
        }

//        fun makePeriodicRequest(): PeriodicWorkRequest {
////            Log.d("SendColorListWorker", "makePeriodicRequest")
//            return PeriodicWorkRequestBuilder<SendColorListWorker>(15, TimeUnit.MINUTES)
//                .build()
//        }
    }
}