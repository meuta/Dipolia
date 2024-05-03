package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetIsLoopingUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(): StateFlow<Boolean> = repository.getIsLooping()
}