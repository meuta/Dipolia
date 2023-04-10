package com.example.dipolia.domain

import com.example.dipolia.domain.entities.LampDomainEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface LampsRepository {

    suspend fun sendFollowMe()

//    fun getLatestLampList(): Flow<List<LampDomainEntity>>
//    suspend fun getLatestLampList(): StateFlow<List<LampDomainEntity>>
    fun getLatestLampList(): SharedFlow<List<LampDomainEntity>>

    fun selectLamp(lampId: String)

    fun unselectLamp()

    fun changeLocalState(set: String, index: Int, value: Double)

    fun saveLampToDb(lampDomainEntity: LampDomainEntity)
    fun saveLampListToDb(list: List<LampDomainEntity>)

}