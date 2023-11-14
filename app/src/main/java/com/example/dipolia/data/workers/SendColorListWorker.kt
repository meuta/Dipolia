package com.example.dipolia.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.domain.useCases.GetConnectedLampsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.RoundingMode

@HiltWorker
class SendColorListWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val sender: UDPClient,
    private val getLampsUseCase: GetConnectedLampsUseCase
) : CoroutineWorker(context, workerParameters) {


    override suspend fun doWork(): Result {
        val isLooping = inputData.getBoolean(KEY_IS_LOOPING_VALUE,false)

        var rabbitColorSpeed = 0.5
        val paceChange = 50
        val paceStay = 300
        var count = 0

        while (true) {
//            Log.d("SendColorListWorker", "while (true)")

            getLampsUseCase().collect { lamps ->
//                Log.d("SendColorListWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")
                Log.d("WO isLooping = ", "$isLooping")
                count += 1
                val modul = paceChange * 2 + paceStay * 2 + 1
                count %= modul
                val mult = when (count) {
                    in 1 .. paceChange -> count
                    in paceChange + 1 .. paceChange + paceStay -> paceChange
                    in paceChange + paceStay + 1 .. (paceChange * 2 + paceStay) -> paceChange * 2 + paceStay + 1 - count
                    in paceChange * 2 + paceStay + 1 .. (paceChange + paceStay) * 2 -> 0
                    else -> 0
                }

                for (lamp in lamps ) {
//                    Log.d("SendColorListWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")

//                    Log.d("SendColorListWorker", "Lamp = ${lamp.id to lamp.c }")

                    if (lamp.c.colors.isNotEmpty()) {
                        val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN))
                        var stringToSend = ""

                        if (lamp.lampType == LampType.DIPOL) {
                            Log.d("SendColorListWorker", "Lamp = ${lamp.id to lamp.c }")

                            var r1Dif = 0.0
                            var r2Dif = 0.0
                            var r3Dif = 0.0
                            if (isLooping) {
                                r1Dif = (lamp.c.colors[0] - lamp.c.colors[3]) / paceChange * mult
                                r2Dif = (lamp.c.colors[1] - lamp.c.colors[4]) / paceChange * mult
                                r3Dif = (lamp.c.colors[2] - lamp.c.colors[5]) / paceChange * mult
                            }

                            val r1 = (BigDecimal(lamp.c.colors[0] - r1Dif).setScale(3, RoundingMode.HALF_DOWN))
                            val g1 = (BigDecimal(lamp.c.colors[1] - r2Dif).setScale(3, RoundingMode.HALF_DOWN))
                            val b1 = (BigDecimal(lamp.c.colors[2] - r3Dif).setScale(3, RoundingMode.HALF_DOWN))
                            val r2 = (BigDecimal(lamp.c.colors[3] + r1Dif).setScale(3, RoundingMode.HALF_DOWN))
                            val g2 = (BigDecimal(lamp.c.colors[4] + r2Dif).setScale(3, RoundingMode.HALF_DOWN))
                            val b2 = (BigDecimal(lamp.c.colors[5] + r3Dif).setScale(3, RoundingMode.HALF_DOWN))

                            stringToSend = "r1=$r1;g1=$g1;b1=$b1;r2=$r2;g2=$g2;b2=$b2;rcs=$rcs"
                            Log.d("SendColorListWorker", "Lamp = (${lamp.id}, count = $count  string =  $stringToSend")

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
//                Log.d("sendColors variance", "string   =  $stringToSend, $address")
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
        const val KEY_IS_LOOPING_VALUE = "key_isLooping"

        fun makeRequest(data: Data): OneTimeWorkRequest {
//            Log.d("SendColorListWorker", "makeOneTimeRequest")
            return OneTimeWorkRequestBuilder<SendColorListWorker>()
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