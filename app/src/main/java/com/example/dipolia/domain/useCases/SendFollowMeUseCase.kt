package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository

class SendFollowMeUseCase(private val repository: LampsRepository) {

    suspend operator fun invoke() = repository.sendFollowMe()
}