package com.example.dipolia.domain.useCases

import androidx.lifecycle.LiveData
import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import kotlinx.coroutines.flow.Flow

//class GetConnectedFiveLightsUseCase (private val repository: DipoliaRepository) {
class GetConnectedFiveLightsUseCase (private val repository: LampsRepository) {

//    operator fun invoke(): LiveData<FiveLightsDomainEntity?> {
//        return repository.getConnectedFiveLights()
//    operator fun invoke(): Flow<FiveLightsDomainEntity?> {
//        return repository.latestFiveLightsLampDomainEntityList()
//    }
}