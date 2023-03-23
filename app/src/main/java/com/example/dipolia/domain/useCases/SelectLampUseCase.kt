package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository

class SelectLampUseCase(private val repository: LampsRepository) {

    operator fun invoke(dipolId: String) = repository.selectLamp(dipolId)
}