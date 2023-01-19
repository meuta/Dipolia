package com.example.dipolia.data

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.database.DipolDbModel
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.domain.ColorComponent
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.Horn

class DipoliaRepositoryImpl(application: Application) : DipoliaRepository {

//    private val dipolsDao = AppDatabase.getInstance(application).dipolsDao()
//    private val mapper = DipoliaMapper()
//
//    private lateinit var dipolListDbModel: List<DipolDbModel>
    override fun getDipolList(): LiveData<List<DipolDomainEntity>> {
        TODO("Not yet implemented")
    }

    override fun selectDipolItem(itemId: String): DipolDomainEntity {
        TODO("Not yet implemented")
    }

    override fun changeLocalState(
        dipolItem: DipolDomainEntity,
        horn: Horn,
        component: ColorComponent,
        componentDiff: Double
    ) {
        TODO("Not yet implemented")
    }

    override fun updateLocalStateList(idStateList: List<Pair<String, String>>) {
        TODO("Not yet implemented")
    }

    override fun changeGlobalState(horn: Horn, colorDiff: Double) {
        TODO("Not yet implemented")
    }

}