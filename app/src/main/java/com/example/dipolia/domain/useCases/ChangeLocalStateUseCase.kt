package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import javax.inject.Inject

class ChangeLocalStateUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(set: String, index: Int, componentValue: Double){
        repository.changeLocalState(set, index, componentValue)
    }

}