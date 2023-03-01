package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.DipoliaRepository

class SelectItemUseCase(private val repository: DipoliaRepository) {

    operator fun invoke(dipolId: String) = repository.selectLamp(dipolId)
}