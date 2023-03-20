package com.example.dipolia.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LampsRemoteDataSource {

    private val lampsApi = LampsApiImpl()

    private val lampDtoList = mutableListOf<LampDto>()

    val myLamps: Flow<List<LampDto>> = flow {
        while (true) {
            val latestLamp = lampsApi.fetchLampDto()
            Log.d("TEST", "latestLamp = ${latestLamp?.id}")
            latestLamp?.let {
                var already = 0
                for (lamp in lampDtoList) {
                    if (lamp.id == it.id) {
                        lamp.lastConnection = System.currentTimeMillis()/1000
                        already = 1
                        break
                    }
                }

                if (already == 0) {

                    lampDtoList.add(latestLamp)
                    Log.d(
                        "TEST",
                        "lampDtoList = ${lampDtoList.map { item -> item.id to item.lastConnection }}"
                    )
                }
            }

            emit(lampDtoList) // Emits the result of the request to the flow
            delay(10) // Suspends the coroutine for some time
        }
    }.flowOn(Dispatchers.IO)
}