package com.example.dipolia.domain

import com.example.dipolia.data.datastore.StreamingPreferences
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
    fun saveLampListToDb()

    suspend fun sendColors()

    fun editLampName(lampId: String, newName: String)

    fun getStreamingState(): Flow<StreamingPreferences>

    suspend fun setLoopSeconds(secondsChange: Double, secondsStay: Double)
    suspend fun setIsLooping(isLooping: Boolean)

    fun getLoopPreferences(): Flow<StreamingPreferences>

}