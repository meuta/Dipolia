package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import javax.inject.Inject

class UnselectLampUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(){
        repository.unselectLamp()
    }
}