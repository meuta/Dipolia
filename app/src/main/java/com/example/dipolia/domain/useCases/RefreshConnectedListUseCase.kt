package com.example.dipolia.domain.useCases

import com.example.dipolia.data.DipoliaRepositoryImpl

class RefreshConnectedListUseCase(private val repository: DipoliaRepositoryImpl) {

    suspend operator fun invoke() = repository.refreshConnectedList()
}
