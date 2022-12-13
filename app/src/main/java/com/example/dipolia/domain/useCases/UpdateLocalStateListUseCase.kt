package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.DipoliaRepository

class UpdateLocalStateListUseCase (private val repository: DipoliaRepository) {

    operator fun invoke(idStateList: List<Pair<String, String>>){
        repository.updateLocalStateList(idStateList)
    }

}