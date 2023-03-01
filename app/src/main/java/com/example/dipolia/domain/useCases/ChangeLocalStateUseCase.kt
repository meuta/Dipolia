package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.DipoliaRepository

class ChangeLocalStateUseCase(private val repository: DipoliaRepository) {

    operator fun invoke(set: String, index: Int, componentValue: Double){
        repository.changeLocalState(set, index, componentValue)
    }

}