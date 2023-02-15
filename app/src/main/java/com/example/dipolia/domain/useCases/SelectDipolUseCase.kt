package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.DipoliaRepository

class SelectDipolUseCase(private val repository: DipoliaRepository) {

    operator fun invoke(dipolId: String) = repository.selectDipolItem(dipolId)
}