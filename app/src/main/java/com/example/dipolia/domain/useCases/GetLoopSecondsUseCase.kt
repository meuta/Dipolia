package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetLoopSecondsUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(): StateFlow<Pair<Double, Double>> = repository.getLoopSeconds()
}