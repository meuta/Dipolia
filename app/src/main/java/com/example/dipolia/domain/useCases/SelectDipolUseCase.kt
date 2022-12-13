package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.DipoliaRepository

class SelectDipolUseCase(private val repository: DipoliaRepository) {

    operator fun invoke(itemId: String): DipolDomainEntity {
        return repository.selectDipolItem(itemId)
    }
}