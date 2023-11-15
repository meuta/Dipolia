package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.presentation.StreamingState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetIsLoopUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(): StateFlow<StreamingState> = repository.getIsLoop()
}