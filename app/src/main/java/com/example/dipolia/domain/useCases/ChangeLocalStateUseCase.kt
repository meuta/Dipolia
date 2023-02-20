package com.example.dipolia.domain.useCases

import com.example.dipolia.domain.ColorComponent
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.Horn

class ChangeLocalStateUseCase(private val repository: DipoliaRepository) {

//    operator fun invoke(
//        dipolItem: DipolDomainEntity,
//        horn: Horn,
//        component: ColorComponent,
//        componentDiff: Double
//    ){
    operator fun invoke(index: Int, componentValue: Double){
//        repository.changeLocalState(dipolItem, horn, component, componentDiff)
        repository.changeLocalState(index, componentValue)
    }

}