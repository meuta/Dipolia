package com.example.dipolia.domain

import com.example.dipolia.domain.entities.LampDomainEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface LampsRepository {

    suspend fun sendFollowMe()
    suspend fun collectList()

    fun getLatestLampList(): SharedFlow<List<LampDomainEntity>>

    fun selectLamp(lampId: String)

    fun unselectLamp()

    fun changeLocalState(id: String, index: Int, value: Double)

    fun saveLampToDb(lampDomainEntity: LampDomainEntity)
    fun saveLampListToDb(list: List<LampDomainEntity>)

    suspend fun sendColors()
}