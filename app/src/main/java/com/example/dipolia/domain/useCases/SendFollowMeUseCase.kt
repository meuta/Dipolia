package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.DipoliaRepository

class SendFollowMeUseCase(private val repository: DipoliaRepository) {

    suspend operator fun invoke() = repository.sendFollowMe()
}