package com.example.dipolia.data

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.database.DipolDbModel
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.network.DipolDto
import com.example.dipolia.data.network.UDPServer
import com.example.dipolia.domain.ColorComponent
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.DipoliaRepository
import com.example.dipolia.domain.Horn

class DipoliaRepositoryImpl(application: Application) : DipoliaRepository {

    private val dipolsDao = AppDatabase.getInstance(application).dipolsDao()
    private val mapper = DipoliaMapper()

    private val receiver = UDPServer()

    override fun receiveLocalModeData() {
        val dipolListDto = mutableListOf<DipolDto>()
        while (true) {              //TODO: must move to another thread
            receiver.receiveStringAndIPFromUDP { string, inetAddress ->
                val ar = string.split(" ")
                if (ar[0] == "dipol") {
                    val id = ar[1]
                    var already = 0
                    for (i in dipolListDto) {
                        if (i.id == id) {
                            already = 1
                            break
                        }
                    }

                    if (already == 0) {
//                    val black = FRGB()
//                    black.fromhsv(0.0, 0.0, 0.0)
                        val dipol = DipolDto(
                            id,
                            inetAddress,
//                        black.clone(),
//                        black.clone(),
//                        black.clone(),
//                        black.clone(),
//                        black.clone(),
//                        black.clone(),
//                        false
                            string
                        )
//                    getDipolColorById(id, dipol.c1, dipol.c2)

                        dipolListDto.add(dipol)
//                    refreshRecyclerView()
                    }
                }
            }
        }
    }

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