package com.example.dipolia.domain

import androidx.lifecycle.LiveData

interface DipoliaRepository {

    suspend fun sendFollowMe()

    suspend fun receiveLocalModeData()

//    suspend fun testSendLocalModeData(dipolID: String, string: String)
    fun testSendLocalModeData(dipolID: String, string: String)

    fun getDipolList(): LiveData<List<DipolDomainEntity>>

    fun selectDipolItem(dipolId: String)

    fun changeLocalState(
        dipolItem: DipolDomainEntity,
        horn: Horn,
        component: ColorComponent,
        componentDiff: Double
    )

    suspend fun editDipolItem(dipolDomainEntity: DipolDomainEntity)

    fun updateLocalStateList(idStateList: List<Pair<String, String>>)

    fun changeGlobalState(horn: Horn, colorDiff: Double)

}
