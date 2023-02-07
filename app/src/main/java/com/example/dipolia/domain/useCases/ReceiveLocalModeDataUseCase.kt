package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.DipoliaRepository

class ReceiveLocalModeDataUseCase(private val repository: DipoliaRepository) {

    suspend operator fun invoke() = repository.receiveLocalModeData()
}