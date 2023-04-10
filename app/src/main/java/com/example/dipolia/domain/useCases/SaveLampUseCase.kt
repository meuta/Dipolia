package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.LampDomainEntity
import javax.inject.Inject

class SaveLampUseCase @Inject constructor(private val repository: LampsRepository) {

//    operator fun invoke(lampDomainEntity: LampDomainEntity){
//        repository.saveLampToDb(lampDomainEntity)
//    }
    operator fun invoke(list: List<LampDomainEntity>){
        repository.saveLampToDb(list)
    }

}