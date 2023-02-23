package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.DipoliaRepository

class UnselectDipolUseCase(private val repository: DipoliaRepository) {

    operator fun invoke(){
        repository.unselectDipol()
    }
}