package com.example.dipolia.data

import android.app.Application
import android.util.Log
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.network.LampDto
import com.example.dipolia.data.network.LampsRemoteDataSource
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class LampsRepositoryImpl(private val application: Application) : LampsRepository {

    private val dipolsDao = AppDatabase.getInstance(application).dipolsDao()
    private val mapper = DipoliaMapper()
    private val lampsRemoteDataSource = LampsRemoteDataSource()
    private val sender = UDPClient()

    private val lampEntityList = mutableListOf<LampDomainEntity>()


    override suspend fun sendFollowMe() {
        while (true) {
            sender.sendUDPSuspend("Follow me")
            delay(100)
        }
    }

    /**
     * Returns the latest list applying transformations on the flow.
     * These operations are lazy and don't trigger the flow. They just transform
     * the current value emitted by the flow at that point in time.
     */
//    override fun latestLampDomainEntityList(): Flow<List<LampDomainEntity>> =
//        lampsRemoteDataSource.myLamps
//            // Intermediate operation to filter the list of lamps
//            .map { lampDtoList ->
//                lampDtoList
//                    // Adding a new lamp to db
//                    .onEach { lampDto ->
////                        Log.d("UDP receiveLocalModeData", "itemToAdd = $itemToAdd")
//                        val itemFromDb = dipolsDao.getLampItemById(lampDto.id)
////                        Log.d("UDP receiveLocalModeData", "itemFromDb = $itemFromDb")
//                        if (itemFromDb == null) {
//                            val itemToAdd = mapper.mapLampDtoToDbModel(lampDto)
//                            dipolsDao.addLampItem(itemToAdd)
//                        } else {
////                        itemToListEntity.c = itemFromDb.colorList
//                            Log.d("TEST", "exist")
//                        }
//                    }
//                    .map { lampDto ->
//                        Log.d("lampDtoList.map", "mapper.mapLampDtoToEntity(lampDto)")
//                        mapper.mapLampDtoToEntity(lampDto)
//                    }
//            }.flowOn(Dispatchers.IO)



    override fun getLatestLampList(): Flow<List<LampDomainEntity>> = lampsRemoteDataSource.myLampDto
        .map { lampDto ->
            var already = 0
//            for (lamp in lampEntityList) {
//                if (lamp.id == lampDto.id) {
//                    lamp.lastConnection = lampDto.lastConnection
//                    already = 1
//                    break
//                }
//            }

            if (lampDto.id in lampEntityList.map { it.id }){
                val lampFromList = lampEntityList.find { lamp -> lamp.id == lampDto.id }
                lampFromList?.lastConnection = lampDto.lastConnection
                already = 1
            }

            if (already == 0) {
                val lampDomainEntity = mapper.mapLampDtoToEntity(lampDto)

                val itemFromDb = dipolsDao.getLampItemById(lampDto.id)
//                        Log.d("UDP receiveLocalModeData", "itemFromDb = $itemFromDb")
                if (itemFromDb == null) {
                    val itemToAdd = mapper.mapLampDtoToDbModel(lampDto)
                    dipolsDao.addLampItem(itemToAdd)
                } else {
                        lampDomainEntity.c = itemFromDb.colorList
//                        lampDomainEntity.c.colors = listOf(0.0, 0.2, 0.4, 0.0, 0.3, 0.0)
                        Log.d("TEST", "exist")
                }

                lampEntityList.add(lampDomainEntity)
                Log.d(
                    "TEST",
                    "lampEntityList = ${lampEntityList.map { item -> item.id to item.lastConnection }}"
                )
            }

            lampEntityList
        }.flowOn(Dispatchers.IO)


//    override fun latestDipolLampDomainEntityList(): Flow<List<DipolDomainEntity>> =
//        lampsRemoteDataSource.myLamps
////    val latestDipolLampDomainEntityList: Flow<List<DipolDomainEntity>> = lampsRemoteDataSource.myLamps
//            // Intermediate operation to filter the list of dipols
//            .map { lampDtoList ->
//                lampDtoList
//                    // Adding a new lamp to db
//                    .onEach { lampDto ->
////                        Log.d("UDP receiveLocalModeData", "itemToAdd = $itemToAdd")
//                        val itemFromDb = dipolsDao.getLampItemById(lampDto.id)
////                        Log.d("UDP receiveLocalModeData", "itemFromDb = $itemFromDb")
//                        if (itemFromDb == null) {
//                            val itemToAdd = mapper.mapLampDtoToDbModel(lampDto)
//                            dipolsDao.addLampItem(itemToAdd)
//                        } else {
////                        itemToListEntity.c = itemFromDb.colorList
////                        Log.d("TEST", "exist")
//                        }
//                    }
//                    .map { lampDto -> mapper.mapLampDtoToEntity(lampDto) }
//                    .filter { it.lampType == LampType.DIPOl && it.connected }
//                    .map { mapper.mapLampEntityToDipolEntity(it) }
//            }.flowOn(Dispatchers.IO)


//    override fun latestFiveLightsLampDomainEntityList(): Flow<FiveLightsDomainEntity> =
//        lampsRemoteDataSource.myLamps
//            // Intermediate operation to filter the list of dipols
//            .map { lampDtoList ->
//                lampDtoList
//                    // Adding a new lamp to db
//                    .onEach { lampDto ->
////                        Log.d("UDP receiveLocalModeData", "itemToAdd = $itemToAdd")
//                        val itemFromDb = dipolsDao.getLampItemById(lampDto.id)
////                        Log.d("UDP receiveLocalModeData", "itemFromDb = $itemFromDb")
//                        if (itemFromDb == null) {
//                            val itemToAdd = mapper.mapLampDtoToDbModel(lampDto)
//                            dipolsDao.addLampItem(itemToAdd)
//                        } else {
////                        itemToListEntity.c = itemFromDb.colorList
////                        Log.d("TEST", "exist")
//                        }
//                    }
//                    .map { lampDto -> mapper.mapLampDtoToEntity(lampDto) }
//                    .filter { it.lampType == LampType.FIVE_LIGHTS && it.connected }
//                    .map { mapper.mapLampEntityToFiveLightsEntity(it) }[0]
//            }.flowOn(Dispatchers.IO)


}