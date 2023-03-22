package com.example.dipolia.domain.useCases

import androidx.lifecycle.LiveData
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.DipoliaRepository

class GetSelectedDipolUseCase(private val repository: DipoliaRepository) {

    operator fun invoke(): LiveData<DipolDomainEntity?> {
        return repository.getSelectedDipol()
    }
}