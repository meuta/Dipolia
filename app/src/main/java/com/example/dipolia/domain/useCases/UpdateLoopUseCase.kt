package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import javax.inject.Inject

class UpdateLoopUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(isLoop : Boolean) = repository.updateIsLoop(isLoop)
}