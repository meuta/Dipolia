package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.LampDomainEntity
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetConnectedLampsUseCase @Inject constructor(private val repository: LampsRepository) {

//    operator fun invoke(): Flow<List<LampDomainEntity>> {
//    suspend operator fun invoke(): StateFlow<List<LampDomainEntity>> {
    operator fun invoke(): SharedFlow<List<LampDomainEntity>> {
//        return repository.latestLampDomainEntityList()
        return repository.getLatestLampList()
    }
}