package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository

class UnselectLampUseCase(private val repository: LampsRepository) {

    operator fun invoke(){
        repository.unselectLamp()
    }
}