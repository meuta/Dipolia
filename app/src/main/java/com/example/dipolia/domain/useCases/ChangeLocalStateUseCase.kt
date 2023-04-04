package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.LampsRepository

class ChangeLocalStateUseCase(private val repository: LampsRepository) {

    suspend operator fun invoke(set: String, index: Int, componentValue: Double){
        repository.changeLocalState(set, index, componentValue)
    }

}