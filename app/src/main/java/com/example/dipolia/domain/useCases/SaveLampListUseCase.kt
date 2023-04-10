package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.LampDomainEntity
import javax.inject.Inject

class SaveLampListUseCase @Inject constructor(private val repository: LampsRepository) {
    operator fun invoke(list: List<LampDomainEntity>){
        repository.saveLampListToDb(list)
    }

}