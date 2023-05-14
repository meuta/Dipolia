package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.LampDomainEntity
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class GetConnectedLampsUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(): SharedFlow<List<LampDomainEntity>> = repository.getLatestLampList()
}