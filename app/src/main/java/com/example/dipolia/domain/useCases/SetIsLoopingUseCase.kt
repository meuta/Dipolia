package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import javax.inject.Inject

class SetIsLoopingUseCase @Inject constructor(private val repository: LampsRepository) {

    suspend operator fun invoke(isLooping: Boolean) = repository.setIsLooping(isLooping)
}