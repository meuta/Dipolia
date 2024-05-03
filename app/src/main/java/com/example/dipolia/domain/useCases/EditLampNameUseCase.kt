package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.LampsRepository
import javax.inject.Inject

class EditLampNameUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(lampId: String, newName: String) =
        repository.editLampName(lampId, newName)
}
