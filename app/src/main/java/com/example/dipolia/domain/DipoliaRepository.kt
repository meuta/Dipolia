package com.example.dipolia.domain

import androidx.lifecycle.LiveData
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType

interface DipoliaRepository {

    suspend fun sendFollowMe()

    suspend fun receiveLocalModeData()

    fun testSendLocalModeData()

    fun getConnectedDipolList(): LiveData<List<DipolDomainEntity>>

    fun selectLamp(lampId: String)

    fun changeLocalState(set: String, index: Int, value: Double)

    suspend fun editDipolItem(dipolDomainEntity: DipolDomainEntity)

    fun updateLocalStateList(idStateList: List<Pair<String, String>>)

    fun changeGlobalState(horn: Horn, colorDiff: Double)

    suspend fun refreshConnectedList()

    fun getSelectedDipol(): LiveData<DipolDomainEntity?>

    fun getConnectedFiveLights(): LiveData<FiveLightsDomainEntity?>

    fun unselectDipol()

    suspend fun dipolsConnectionMonitoring()

    fun workerStartStop()

    fun getIsStreaming(): LiveData<Boolean?>

    fun getLampsTable(): LiveData<List<LampDomainEntity>>

    fun getSelectedLamp(): LiveData<LampDomainEntity?>

    fun unselectLamp()

    fun getSelectedConnectedLampType(): LiveData<LampType?>

}
