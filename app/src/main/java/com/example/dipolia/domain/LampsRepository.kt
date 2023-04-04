package com.example.dipolia.domain

import com.example.dipolia.domain.entities.LampDomainEntity
import kotlinx.coroutines.flow.Flow

interface LampsRepository {

    suspend fun sendFollowMe()

    fun getLatestLampList(): Flow<List<LampDomainEntity>>

    fun selectLamp(lampId: String)

    fun unselectLamp()

    suspend fun changeLocalState(set: String, index: Int, value: Double)

}