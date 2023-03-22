package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import kotlinx.coroutines.flow.Flow

class GetConnectedLampsUseCase(private val repository: LampsRepository) {

    operator fun invoke(): Flow<List<LampDomainEntity>> {
//        return repository.latestLampDomainEntityList()
        return repository.getLatestLampList()
    }
}