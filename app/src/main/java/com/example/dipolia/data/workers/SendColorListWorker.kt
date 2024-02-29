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
import kotlinx.coroutines.coroutineScope
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


    private var isLooping = false
    private var secChange = 0
    private var secStay = 0

    override suspend fun doWork(): Result = coroutineScope {

        var rabbitColorSpeed = 0.5
        val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN))
        var stringToSend = ""

        var count = 0
        var period = 0
        var factor: Int
        val dipolDif = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        var tints: List<BigDecimal>

        this.launch {
            getIsLoopingUseCase().collectLatest { isL ->
                Log.d("getIsLoopingUseCase ", "isLooping = $isL")
                isLooping = isL
            }
        }

        this.launch {
            getLoopSecondsUseCase().collectLatest { pair ->
                Log.d("getLoopSecondsUseCase ", "secondsChange = ${pair.first}")
                Log.d("getLoopSecondsUseCase ", "secondsStay = ${pair.second}")
                secChange = (pair.first * 10).toInt()
                secStay = (pair.second * 10).toInt()
                period = (secChange + secStay) * 2
            }
        }

        this.launch {
            getLampsUseCase().collectLatest { lamps ->
                Log.d("SendColorListWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")
//                Log.d("getLampsUseCase().collect ", "isLooping = $isLooping")
//                Log.d("getLampsUseCase().collect ", "secondsChange = $secondsChange")
//                Log.d("getLampsUseCase().collect ", "secondsStay = $secondsStay")

                if (isLooping && (secChange > 0)) {
                    count += 1
                    count %= period
                }

                for (lamp in lamps) {
//                    Log.d("SendColorListWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")
//                    Log.d("SendColorListWorker", "Lamp = ${lamp.id to lamp.c }")

                    if (lamp.c.colors.isNotEmpty()) {


                        if (lamp.lampType == LampType.DIPOL) {
                            Log.d("SendColorListWorker", "Lamp = ${lamp.id to lamp.c}")


                            if (isLooping && (secChange > 0)) {
                                Log.d(
                                    "isLooping && (paceChange > 0) ",
                                    "isLooping = $isLooping"
                                )
                                Log.d(
                                    "isLooping && (paceChange > 0) ",
                                    "secondsChange = $secChange"
                                )
                                Log.d(
                                    "isLooping && (paceChange > 0) ",
                                    "secondsStay = $secStay"
                                )

                                factor = when (count) {
                                    in 1..secChange -> count
                                    in secChange + 1..secChange + secStay -> secChange
                                    in secChange + secStay + 1..(secChange * 2 + secStay) -> secChange * 2 + secStay + 1 - count
                                    in secChange * 2 + secStay + 1..(secChange + secStay) * 2 -> 0
                                    else -> 0
                                }

                                for (i in dipolDif.indices) {
                                    dipolDif[i] =
                                        (lamp.c.colors[i] - lamp.c.colors[(i + 3) % 6]) / secChange * factor
                                }
                            }
                            tints = lamp.c.colors.withIndex().map {
                                BigDecimal(it.value - dipolDif[it.index]).setScale(
                                    3,
                                    RoundingMode.HALF_DOWN
                                )
                            }
                            stringToSend =
                                "r1=${tints[0]};g1=${tints[1]};b1=${tints[2]};r2=${tints[3]};g2=${tints[4]};b2=${tints[5]};rcs=$rcs"

                            Log.d(TAG, "doWork: Lamp = ${lamp.id}, ip = ${lamp.ip}, count = $count  string =  $stringToSend")

                        } else if (lamp.lampType == LampType.FIVE_LIGHTS) {
                            tints = lamp.c.colors.map {
                                BigDecimal(it).setScale(
                                    3,
                                    RoundingMode.HALF_DOWN
                                )
                            }
                            stringToSend =
                                "r=${tints[0]};g=${tints[1]};b=${tints[2]};w=${tints[3]};u=${tints[4]}};rcs=$rcs"

                            Log.d(TAG, "doWork: Lamp = ${lamp.id}, ip = ${lamp.ip}, string =  $stringToSend")
                        }

                        sender.sendUDPSuspend(stringToSend, sender.getInetAddressByName(lamp.ip))
                    }
                }
            }
        }

        Result.success()
    }


    companion object {

        const val TAG = "SendColorListWorker"
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