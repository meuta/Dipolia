package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.LampsRepository

//class SendFollowMeUseCase(private val repository: DipoliaRepository) {
class SendFollowMeUseCase(private val repository: LampsRepository) {

//    suspend operator fun invoke() = repository.sendFollowMe()
    suspend operator fun invoke() = repository.sendFollowMe()
}