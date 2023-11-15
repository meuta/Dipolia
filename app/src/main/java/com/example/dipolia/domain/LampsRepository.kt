package com.example.dipolia.domain

import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.presentation.StreamingState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface LampsRepository {

    suspend fun sendFollowMe()
    suspend fun collectList()

    fun getLatestLampList(): SharedFlow<List<LampDomainEntity>>

    fun selectLamp(lampId: String)

    fun unselectLamp()

    fun changeLocalState(id: String, index: Int, value: Double)

    fun saveLampToDb(lampDomainEntity: LampDomainEntity)
    fun saveLampListToDb()

    suspend fun sendColors()

    fun editLampName(lampId: String, newName: String)

    fun getIsLoop(): StateFlow<StreamingState>
    fun updateIsLoop(isLooping: Boolean)
}