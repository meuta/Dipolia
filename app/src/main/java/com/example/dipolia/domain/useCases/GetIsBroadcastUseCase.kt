package com.example.dipolia.domain.useCases

import androidx.lifecycle.LiveData
import com.example.dipolia.data.DipoliaRepositoryImpl

class GetIsBroadcastUseCase(private val repository: DipoliaRepositoryImpl) {

    operator fun invoke(): LiveData<Boolean> {
        return repository.getIsBroadcast()
    }
}
