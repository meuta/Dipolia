package com.example.dipolia.domain

import androidx.lifecycle.LiveData
import com.example.dipolia.domain.entities.FiveLightsDomainEntity

interface DipoliaRepository {

    suspend fun sendFollowMe()

    suspend fun receiveLocalModeData()

    fun testSendLocalModeData()

    fun getConnectedDipolList(): LiveData<List<DipolDomainEntity>>

    fun selectDipolItem(dipolId: String)

//    fun changeLocalState(
//        dipolItem: DipolDomainEntity,
//        horn: Horn,
//        component: ColorComponent,
//        componentDiff: Double
//    )
    fun changeLocalState(set: String, index: Int, value: Double)

    suspend fun editDipolItem(dipolDomainEntity: DipolDomainEntity)

    fun updateLocalStateList(idStateList: List<Pair<String, String>>)

    fun changeGlobalState(horn: Horn, colorDiff: Double)

    suspend fun refreshConnectedList()

    fun getSelectedDipol(): LiveData<DipolDomainEntity?>

    fun getFiveLights(): LiveData<FiveLightsDomainEntity?>

    fun unselectDipol()

    suspend fun dipolsConnectionMonitoring()

//    fun workerStartStop()
    fun workerStartStop()

    fun getIsBroadcast(): LiveData<Boolean>

}
