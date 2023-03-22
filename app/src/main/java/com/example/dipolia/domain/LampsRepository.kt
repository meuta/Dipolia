package com.example.dipolia.domain

import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import kotlinx.coroutines.flow.Flow

interface LampsRepository {

    suspend fun sendFollowMe()
//    fun latestLampDomainEntityList(): Flow<List<LampDomainEntity>>
    fun getLatestLampList(): Flow<List<LampDomainEntity>>

//    fun latestDipolLampDomainEntityList(): Flow<List<DipolDomainEntity>>

//    fun latestFiveLightsLampDomainEntityList(): Flow<List<FiveLightsDomainEntity>>
//    fun latestFiveLightsLampDomainEntityList(): Flow<FiveLightsDomainEntity>

}