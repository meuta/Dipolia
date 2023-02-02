package com.example.dipolia.domain

import androidx.lifecycle.LiveData

interface DipoliaRepository {

    suspend fun receiveLocalModeData()

    fun getDipolList(): LiveData<List<DipolDomainEntity>>

    fun selectDipolItem(itemId: String): DipolDomainEntity

    fun changeLocalState(
        dipolItem: DipolDomainEntity,
        horn: Horn,
        component: ColorComponent,
        componentDiff: Double
    )

    fun updateLocalStateList(idStateList: List<Pair<String, String>>)

    fun changeGlobalState(horn: Horn, colorDiff: Double)

}
