package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.LampsRepository
import kotlinx.coroutines.flow.Flow

class GetDipolListUseCase(private val repository: LampsRepository) {


//    operator fun invoke(): Flow<List<DipolDomainEntity>> {
//        return repository.latestDipolLampDomainEntityList()
//    }
}