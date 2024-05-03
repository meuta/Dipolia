package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import javax.inject.Inject

class SelectLampUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(dipolId: String) = repository.selectLamp(dipolId)
}