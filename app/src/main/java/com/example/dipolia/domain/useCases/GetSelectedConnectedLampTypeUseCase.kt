package com.example.dipolia.domain.useCases

import androidx.lifecycle.LiveData
import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.entities.LampType

class GetSelectedConnectedLampTypeUseCase(private val repository: DipoliaRepository) {

    operator fun invoke(): LiveData<LampType?> {
        return repository.getSelectedConnectedLampType()
    }
}