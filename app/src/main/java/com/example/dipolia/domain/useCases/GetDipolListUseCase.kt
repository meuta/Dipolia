package com.example.dipolia.domain.useCases

import androidx.lifecycle.LiveData
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.DipoliaRepository

class GetDipolListUseCase(private val repository: DipoliaRepository) {

    operator fun invoke(): LiveData<List<DipolDomainEntity>> {
        return repository.getDipolList()
    }
}