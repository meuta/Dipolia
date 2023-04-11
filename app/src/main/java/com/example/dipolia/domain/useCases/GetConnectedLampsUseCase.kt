package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.LampDomainEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConnectedLampsUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(): Flow<List<LampDomainEntity>> {

        return repository.getLatestLampList()
    }
}