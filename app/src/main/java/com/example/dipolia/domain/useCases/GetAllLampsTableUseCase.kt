package com.example.dipolia.domain.useCases

import androidx.lifecycle.LiveData
import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.entities.LampDomainEntity

class GetAllLampsTableUseCase(private val repository: DipoliaRepository) {

    operator fun invoke(): LiveData<List<LampDomainEntity>> {
        return repository.getLampsTable()
    }
}