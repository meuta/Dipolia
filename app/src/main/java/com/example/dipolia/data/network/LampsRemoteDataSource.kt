package com.example.dipolia.data.network

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class LampsRemoteDataSource @Inject constructor() {

//    private val lampsApi = LampsApiImpl(UDPServer())            //is it normal?
    private val lampsApi = LampsApiImpl()

//    private val lampDtoList = mutableListOf<LampDto>()

    val myLampDto: Flow<LampDto> = flow {
        while (true) {
            val latestLamp = lampsApi.fetchLampDto()
            Log.d("TEST", "latestLamp = ${latestLamp?.id}")
            latestLamp?.let {
                emit(it) // Emits the result of the request to the flow
                delay(100) // Suspends the coroutine for some time
            }
        }
    }
//    private val _myLampDto = MutableSharedFlow<LampDto>() // 1
//
//    val myLampDto: SharedFlow<LampDto> = _myLampDto//.asSharedFlow() // 2
//
//    suspend fun emitFlow(){
//        while (true) {
//            val latestLamp = lampsApi.fetchLampDto()
//            Log.d("TEST", "latestLamp = ${latestLamp?.id}")
//            latestLamp?.let {
//                _myLampDto.emit(it) // Emits the result of the request to the flow
//                delay(100) // Suspends the coroutine for some time
//            }
//        }
//    }
//    val myLampDto: SharedFlow<LampDto> = flow {
//        while (true) {
//            val latestLamp = lampsApi.fetchLampDto()
//            Log.d("TEST", "latestLamp = ${latestLamp?.id}")
//            latestLamp?.let {
//                emit(it) // Emits the result of the request to the flow
//                delay(100) // Suspends the coroutine for some time
//            }
//        }
//    } as SharedFlow<LampDto>


//    val myLamps: Flow<List<LampDto>> = flow {
//        while (true) {
//            val latestLamp = lampsApi.fetchLampDto()
//            Log.d("TEST", "latestLamp = ${latestLamp?.id}")
//            latestLamp?.let {
//                var already = 0
//                for (lamp in lampDtoList) {
//                    if (lamp.id == it.id) {
//                        lamp.lastConnection = it.lastConnection
//                        already = 1
//                        break
//                    }
//                }
//
//                if (already == 0) {
//
//                    lampDtoList.add(latestLamp)
//                    Log.d(
//                        "TEST",
//                        "lampDtoList = ${lampDtoList.map { item -> item.id to item.lastConnection }}"
//                    )
//                }
//                emit(lampDtoList) // Emits the result of the request to the flow
//                delay(100) // Suspends the coroutine for some time
//            }
//
//        }
//    }
}