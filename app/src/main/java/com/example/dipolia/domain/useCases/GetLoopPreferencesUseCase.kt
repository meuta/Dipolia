package com.example.dipolia.domain.useCases

import com.example.dipolia.data.datastore.StreamingPreferences
import com.example.dipolia.domain.LampsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLoopPreferencesUseCase @Inject constructor(private val repository: LampsRepository) {

    operator fun invoke(): Flow<StreamingPreferences> = repository.getLoopPreferences()
}