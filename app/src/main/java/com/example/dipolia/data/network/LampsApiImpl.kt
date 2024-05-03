package com.example.dipolia.data.network

import android.util.Log
import com.example.dipolia.domain.entities.LampType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LampsApiImpl @Inject constructor(private val receiver: UDPServer): LampsApi {

    private var fiveLightsCounter = 0

    override suspend fun fetchLampDto(): LampDto? {
        var lampDto : LampDto? = null
        val receivedData = receiver.receiveStringAndIPFromUDP()
//        Log.d("receiveLocalModeData", "Pair received: $receivedData")

        receivedData?.let {

            val ar = it.first.split(" ")
            val lampTypeString = ar[0]
//            Log.d("receiveLocalModeData", "lampTypeString = $lampTypeString")

            if (lampTypeString == DIPOL || lampTypeString == FIVE_LIGHTS) {
//                Log.d("receiveLocalModeData", "ip = ${it.second}, lampType = $lampTypeString")
                if (lampTypeString == FIVE_LIGHTS) {
                    fiveLightsCounter++
                    fiveLightsCounter %= 2

//                    Log.d("receiveLocalModeData", "fiveLightsCounter = $fiveLightsCounter")
                }
                if (lampTypeString == DIPOL || (fiveLightsCounter == 0)) {
//                    Log.d("receiveLocalModeData", "inside if: ip = ${it.second}, lampType = $lampTypeString")
                    val id = ar[1].substring(0, ar[1].length - 1)

                    val lampType = when (lampTypeString) {
                        DIPOL -> LampType.DIPOL
                        FIVE_LIGHTS -> LampType.FIVE_LIGHTS
                        else -> LampType.UNKNOWN_LAMP_TYPE
                    }
                    lampDto = LampDto(id, it.second, lampType, System.currentTimeMillis() / 1000)
                }
            }
        }
        return lampDto
    }

    companion object {

        private const val DIPOL = "dipol"
        private const val FIVE_LIGHTS = "5lights"
    }
}