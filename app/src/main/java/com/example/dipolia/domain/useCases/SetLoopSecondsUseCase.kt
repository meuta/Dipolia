package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import javax.inject.Inject

class SetLoopSecondsUseCase @Inject constructor(private val repository: LampsRepository) {

    suspend operator fun invoke(secondsChange: Double, secondsStay: Double) =
        repository.setLoopSeconds(secondsChange, secondsStay)
}