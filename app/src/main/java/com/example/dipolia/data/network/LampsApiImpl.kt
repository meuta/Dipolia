package com.example.dipolia.data.network

import android.util.Log
import com.example.dipolia.domain.entities.LampType
import javax.inject.Inject

class LampsApiImpl @Inject constructor(private val receiver: UDPServer): LampsApi {


    override suspend fun fetchLampDto(): LampDto? {
        var lampDto : LampDto? = null
        val receivedData = receiver.receiveStringAndIPFromUDP()
//        Log.d("receiveLocalModeData", "Pair received: $receivedData")

        receivedData?.let {

            val ar = it.first.split(" ")
            val lampTypeString = ar[0]
//            Log.d("receiveLocalModeData", "lampTypeString = $lampTypeString")

            if (lampTypeString == "dipol" || lampTypeString == "5lights") {

//                Log.d("receiveLocalModeData", "inside if lampTypeString = $lampTypeString")
                val id = ar[1].substring(0, ar[1].length - 1)

                val lampType = when (lampTypeString) {
                    "dipol" -> LampType.DIPOL
                    "5lights" -> LampType.FIVE_LIGHTS
                    else -> LampType.UNKNOWN_LAMP_TYPE
                }
                lampDto = LampDto(id, it.second, lampType, System.currentTimeMillis()/1000)
            }
        }
        return lampDto
    }
}