package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.presentation.StreamingState
import javax.inject.Inject

class UpdateStreamingStateUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(streamState: StreamingState) = repository.updateStreamingState(streamState)
}