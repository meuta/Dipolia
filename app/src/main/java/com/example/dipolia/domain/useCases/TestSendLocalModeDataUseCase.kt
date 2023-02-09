package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.ColorComponent
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.Horn

class TestSendLocalModeDataUseCase(private val repository: DipoliaRepository) {

    suspend operator fun invoke(dipolID: String, string: String){
        repository.testSendLocalModeData(dipolID, string)
    }
}