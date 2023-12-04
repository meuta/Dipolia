package com.example.dipolia.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.domain.useCases.GetConnectedLampsUseCase
import com.example.dipolia.domain.useCases.GetIsLoopingUseCase
import com.example.dipolia.domain.useCases.GetLoopSecondsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@HiltWorker
class SendColorListWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val sender: UDPClient,
    private val getLampsUseCase: GetConnectedLampsUseCase,
    private val getIsLoopingUseCase: GetIsLoopingUseCase,
    private val getLoopSecondsUseCase: GetLoopSecondsUseCase,
) : CoroutineWorker(context, workerParameters) {

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun doWork(): Result {
        var isLooping = false
        var secondsChange = 0
        var secondsStay = 0

        scope.launch {
            getIsLoopingUseCase().collectLatest {
                Log.d("getIsLoopingUseCase ", "isLooping = $it")
                isLooping = it
            }
        }

        scope.launch {
            getLoopSecondsUseCase().collectLatest {
                Log.d("getLoopSecondsUseCase ", "secondsChange = ${it.first}")
                Log.d("getLoopSecondsUseCase ", "secondsStay = ${it.second}")
                secondsChange = (it.first*10).toInt()
                secondsStay = (it.second*10).toInt()
            }
        }

        var rabbitColorSpeed = 0.5

        var count = 0

        while (true) {
//            Log.d("SendColorListWorker", "while (true)")

            getLampsUseCase().collect { lamps ->
//                Log.d("SendColorListWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")
//                Log.d("getLampsUseCase().collect ", "isLooping = $isLooping")
//                Log.d("getLampsUseCase().collect ", "secondsChange = $secondsChange")
//                Log.d("getLampsUseCase().collect ", "secondsStay = $secondsStay")

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

                            val rDif = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

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

                                for (i in rDif.indices){
                                    rDif[i] = (lamp.c.colors[i] - lamp.c.colors[(i + 3) % 6] )/ secondsChange * factor
                                }
                            }
                            val tints = lamp.c.colors.withIndex().map { BigDecimal(it.value - rDif[it.index]).setScale(3, RoundingMode.HALF_DOWN) }
                            stringToSend = "r1=${tints[0]};g1=${tints[1]};b1=${tints[2]};r2=${tints[3]};g2=${tints[4]};b2=${tints[5]};rcs=$rcs"

                            Log.d(
                                "SendColorListWorker",
                                "Lamp = (${lamp.id}, count = $count  string =  $stringToSend"
                            )

                        } else if (lamp.lampType == LampType.FIVE_LIGHTS) {
                            val tints = lamp.c.colors.map { BigDecimal(it).setScale(3, RoundingMode.HALF_DOWN) }
                            stringToSend = "r=${tints[0]};g=${tints[1]};b=${tints[2]};w=${tints[3]};u=${tints[4]}};rcs=$rcs"

                            Log.d("sendColors", "string   =  $stringToSend")
                        }
                        val address = sender.getInetAddressByName(lamp.ip)
                        sender.sendUDPSuspend(stringToSend, address)
                    }
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