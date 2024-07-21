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
    private var timeChange = 0
    private var timeStay = 0

    override suspend fun doWork(): Result = coroutineScope {

        var rabbitColorSpeed = 0.5
        val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN))
        var stringToSend = ""

        var count = -1
        var period = 0

        val dipolY = MutableList(5) { 0.0 }
        var tints: List<BigDecimal>

        this.launch {
            getIsLoopingUseCase().collectLatest { isL ->
//                Log.d("getIsLoopingUseCase ", "isLooping = $isL")
                isLooping = isL
            }
        }

        this.launch {
            getLoopSecondsUseCase().collectLatest { pair ->
//                Log.d("getLoopSecondsUseCase ", "secondsChange = ${pair.first}")
//                Log.d("getLoopSecondsUseCase ", "secondsStay = ${pair.second}")
                timeChange = (pair.first * 10).toInt()
                timeStay = (pair.second * 10).toInt()
                period = (timeChange + timeStay) * 2
            }
        }

        this.launch {
            getLampsUseCase().collectLatest { lamps ->
//                Log.d("SendColorListWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")
//                Log.d("getLampsUseCase().collect ", "isLooping = $isLooping")
//                Log.d("getLampsUseCase().collect ", "secondsChange = $secondsChange")
//                Log.d("getLampsUseCase().collect ", "secondsStay = $secondsStay")

                if (isLooping && (timeChange > 0)) {
                    count = (count + 1) % period
                }

                for (lamp in lamps) {
//                    Log.d("SendColorListWorker", "LampDomainEntityList = ${lamps.map { it.id to it.c }}")
//                    Log.d("SendColorListWorker", "Lamp = ${lamp.id to lamp.c }")

                    if (lamp.c.colors.isNotEmpty()) {


                        if (lamp.lampType == LampType.DIPOL) {
//                            Log.d("SendColorListWorker", "Lamp = ${lamp.id to lamp.c}")


                            if (isLooping && (timeChange > 0)) {
//                                Log.d("isLooping && (paceChange > 0) ", "isLooping = $isLooping")
//                                Log.d("isLooping && (paceChange > 0) ", "secondsChange = $timeChange")
//                                Log.d("isLooping && (paceChange > 0) ", "secondsStay = $timeStay")

                                for (i in dipolY.indices) {
                                    dipolY[i] = when (count) {
                                        in 1..timeChange / 2 ->
                                            lamp.c.colors[i] - 2 * lamp.c.colors[i] * count / timeChange
                                        in timeChange / 2 + 1 .. timeChange ->
                                            2 * lamp.c.colors[(i + 3) % 6] * count / timeChange - lamp.c.colors[(i + 3) % 6]
                                        in timeChange + 1..timeChange + timeStay ->
                                            lamp.c.colors[(i + 3) % 6]
                                        in timeChange + timeStay + 1..timeChange * 3 / 2 + timeStay ->
                                            (timeChange * 3 / 2 + timeStay) * 2 * lamp.c.colors[(i + 3) % 6] / timeChange - 2 * lamp.c.colors[(i + 3) % 6] / timeChange * count
                                        in timeChange * 3 / 2 + timeStay + 1 .. timeChange * 2 + timeStay ->
                                            2 * lamp.c.colors[i] / timeChange * count - (timeChange * 3 / 2 + timeStay) * 2 * lamp.c.colors[i] / timeChange
                                        in timeChange * 2 + timeStay + 1..(timeChange + timeStay) * 2 ->
                                            lamp.c.colors[i]
                                        else -> lamp.c.colors[i]
                                    }
                                }

                            }
                            tints = lamp.c.colors.withIndex().map {
//                                BigDecimal(it.value - dipolDif[it.index]).setScale(
                                BigDecimal(if (isLooping) dipolY[it.index] else it.value).setScale(
                                    3,
                                    RoundingMode.HALF_DOWN
                                )
                            }
                            stringToSend =
                                "r1=${tints[0]};g1=${tints[1]};b1=${tints[2]};r2=${tints[3]};g2=${tints[4]};b2=${tints[5]};rcs=$rcs"

//                            Log.d(TAG, "doWork: Lamp = ${lamp.id}, ip = ${lamp.ip}, count = $count  string =  $stringToSend")

                        } else if (lamp.lampType == LampType.FIVE_LIGHTS) {
                            tints = lamp.c.colors.map {
                                BigDecimal(it).setScale(
                                    3,
                                    RoundingMode.HALF_DOWN
                                )
                            }
                            stringToSend =
                                "r=${tints[0]};g=${tints[1]};b=${tints[2]};w=${tints[3]};u=${tints[4]};rcs=$rcs"

//                            Log.d(TAG, "doWork: Lamp = ${lamp.id}, ip = ${lamp.ip}, string =  $stringToSend")
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