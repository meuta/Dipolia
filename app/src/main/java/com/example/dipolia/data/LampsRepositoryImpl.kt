package com.example.dipolia.data

import android.app.Application
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.network.LampDto
import com.example.dipolia.data.network.LampsRemoteDataSource
import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LampsRepositoryImpl(private val application: Application) : LampsRepository {

    private val mapper = DipoliaMapper()
    private val lampsRemoteDataSource = LampsRemoteDataSource()

    /**
     * Returns the latest lampDtoList applying transformations on the flow.
     * These operations are lazy and don't trigger the flow. They just transform
     * the current value emitted by the flow at that point in time.
     */
//    val latestDipolLampDtoList: Flow<List<LampDto>> = lampsRemoteDataSource.myLamps
    val latestDipolLampDomainEntityList: Flow<List<LampDomainEntity>> = lampsRemoteDataSource.myLamps
        // Intermediate operation to filter the list of dipols
        .map { lamps ->
            lamps
                .map { lampDto -> mapper.mapLampDtoToEntity(lampDto) }
                .filter { it.lampType == LampType.DIPOl && it.connected }
        }


}