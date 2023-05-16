package com.example.dipolia.data.network

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class LampsRemoteDataSource @Inject constructor(private val lampsApi: LampsApi) {


    val myLampDto: Flow<LampDto> = flow {
        while (true) {
            val latestLamp = lampsApi.fetchLampDto()
            Log.d("TEST", "latestLamp = ${latestLamp?.id}")
            latestLamp?.let {
                emit(it) // Emits the result of the request to the flow
//                delay(1000) // Suspends the coroutine for some time
            }
        }
    }

}