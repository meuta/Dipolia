package com.example.dipolia.domain.useCases

import com.example.dipolia.data.DipoliaRepositoryImpl

class WorkerStartStopUseCase(private val repository: DipoliaRepositoryImpl) {

    operator fun invoke() {
        repository.workerStartStop()
    }
}
