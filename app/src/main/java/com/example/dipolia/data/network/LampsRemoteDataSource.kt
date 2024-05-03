package com.example.dipolia.data.network

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LampsRemoteDataSource @Inject constructor(private val lampsApi: LampsApi) {

    private var latestLamp : LampDto? = null
    val myLampDto: Flow<LampDto> = flow {
        while (true) {
            latestLamp = lampsApi.fetchLampDto()
//            Log.d("TEST", "latestLamp = ${latestLamp?.id}")
            latestLamp?.let {
                emit(it)
            }
        }
    }
}