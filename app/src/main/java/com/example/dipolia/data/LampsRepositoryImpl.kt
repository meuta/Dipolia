package com.example.dipolia.data

import android.app.Application
import android.util.Log
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.network.LampDto
import com.example.dipolia.data.network.LampsRemoteDataSource
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.LampType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class LampsRepositoryImpl(private val application: Application) : LampsRepository {

    private val dipolsDao = AppDatabase.getInstance(application).dipolsDao()
    private val mapper = DipoliaMapper()
    private val lampsRemoteDataSource = LampsRemoteDataSource()


    /**
     * Returns the latest list applying transformations on the flow.
     * These operations are lazy and don't trigger the flow. They just transform
     * the current value emitted by the flow at that point in time.
     */
    fun latestDipolLampDomainEntityList(): Flow<List<DipolDomainEntity>> = lampsRemoteDataSource.myLamps
//    val latestDipolLampDomainEntityList: Flow<List<DipolDomainEntity>> = lampsRemoteDataSource.myLamps
        // Intermediate operation to filter the list of dipols
        .map { lampDtoList ->
            lampDtoList
                    // Adding a new lamp to db
                .onEach { lampDto ->
//                        Log.d("UDP receiveLocalModeData", "itemToAdd = $itemToAdd")
                    val itemFromDb = dipolsDao.getLampItemById(lampDto.id)
//                        Log.d("UDP receiveLocalModeData", "itemFromDb = $itemFromDb")
                    if (itemFromDb == null) {
                        val itemToAdd = mapper.mapLampDtoToDbModel(lampDto)
                        dipolsDao.addLampItem(itemToAdd)
                    }else {
//                        itemToListEntity.c = itemFromDb.colorList
                        Log.d("TEST", "exist")
                    }
                }
                .map { lampDto -> mapper.mapLampDtoToEntity(lampDto) }
                .filter { it.lampType == LampType.DIPOl && it.connected }
                .map { mapper.mapLampEntityToDipolEntity(it) }
        }.flowOn(Dispatchers.IO)


    val latestLampDtoList: Flow<List<LampDto>> = lampsRemoteDataSource.myLamps
//        .map { lampDtoList ->
//        lampDtoList.map { mapper.mapLampDtoToEntity(it) }
//    }

}