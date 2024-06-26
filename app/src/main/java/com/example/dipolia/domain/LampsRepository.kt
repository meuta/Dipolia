package com.example.dipolia.domain

import com.example.dipolia.domain.entities.LampDomainEntity
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface LampsRepository {

    fun collectList()
    fun getLatestLampList(): SharedFlow<List<LampDomainEntity>>

    fun selectLamp(lampId: String)

    fun unselectLamp()

    fun changeLocalState(id: String, index: Int, value: Double)

    fun saveLampToDb(lampDomainEntity: LampDomainEntity)
    fun saveLampListToDb()

    suspend fun sendColors()

    fun editLampName(lampId: String, newName: String)


    suspend fun setLoopSeconds(secondsChange: Double, secondsStay: Double)
    suspend fun setIsLooping(isLooping: Boolean)

    fun getIsLooping(): StateFlow<Boolean>
    fun getLoopSeconds(): StateFlow<Pair<Double, Double>>


}