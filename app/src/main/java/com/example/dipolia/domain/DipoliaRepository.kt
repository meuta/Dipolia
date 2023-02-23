package com.example.dipolia.domain

import androidx.lifecycle.LiveData

interface DipoliaRepository {

    suspend fun sendFollowMe()

    suspend fun receiveLocalModeData()

    fun testSendLocalModeData()

    fun getDipolList(): LiveData<List<DipolDomainEntity>>

    fun selectDipolItem(dipolId: String)

//    fun changeLocalState(
//        dipolItem: DipolDomainEntity,
//        horn: Horn,
//        component: ColorComponent,
//        componentDiff: Double
//    )
    fun changeLocalState(index: Int, value: Double)

    suspend fun editDipolItem(dipolDomainEntity: DipolDomainEntity)

    fun updateLocalStateList(idStateList: List<Pair<String, String>>)

    fun changeGlobalState(horn: Horn, colorDiff: Double)

    suspend fun refreshConnectedList()

    fun getSelectedDipol(): LiveData<DipolDomainEntity?>

    fun unselectDipol()

}
