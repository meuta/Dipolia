package com.example.dipolia.domain.useCases

import androidx.lifecycle.LiveData
import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.entities.FiveLightsDomainEntity

class GetFiveLightsUseCase (private val repository: DipoliaRepository) {

    operator fun invoke(): LiveData<FiveLightsDomainEntity?> {
        return repository.getFiveLights()
    }
}