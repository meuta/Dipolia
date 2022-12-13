package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.Horn

class ChangeGlobalStateUseCase (private val repository: DipoliaRepository) {

    operator fun invoke(horn: Horn, colorDiff: Double){
        repository.changeGlobalState(horn, colorDiff)
    }

}