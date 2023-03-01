package com.example.dipolia.domain.useCases

import androidx.lifecycle.LiveData
import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.entities.LampDomainEntity

class GetSelectedLampUseCase(private val repository: DipoliaRepository) {

    operator fun invoke(): LiveData<LampDomainEntity?> {
        return repository.getSelectedLamp()
    }
}