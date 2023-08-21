package com.example.dipolia.data

import android.util.Log

import com.example.dipolia.data.database.ColorList
import com.example.dipolia.data.database.DipolsDao
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.network.LampsRemoteDataSource
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LampsRepositoryImpl @Inject constructor(
    private val dipolsDao: DipolsDao,
    private val mapper: DipoliaMapper,
    private val lampsRemoteDataSource: LampsRemoteDataSource,
    private val sender: UDPClient
) : LampsRepository {

    private val lampEntityList = mutableListOf<LampDomainEntity>()

    private val lampListFlow: SharedFlow<List<LampDomainEntity>> = flow {
        while (true) {
            val latestLampList = lampEntityList

            Log.d("TEST", "latestLampList = ${latestLampList.map { listOf(it.id, it.lampName, it.lastConnection) }}")
            emit(latestLampList)
            delay(100)
        }
    }.shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily)

    override suspend fun sendFollowMe() {
        while (true) {
            sender.sendUDPSuspend("Follow me")
            delay(1000)
        }
    }

    override suspend fun collectList() {
        while (true) {
            lampsRemoteDataSource.myLampDto.collect { lampDto ->

                if (lampDto.id in lampEntityList.map { it.id }) {
//                    Log.d("collectList","lampDto.id in lampEntityList ${lampDto.id}")
                    val lampFromList = lampEntityList.find { lamp -> lamp.id == lampDto.id }
                    val lampFromListIndex = lampEntityList.indexOf(lampFromList)
                    lampFromList?.let {
                        it.lastConnection = lampDto.lastConnection

                        lampEntityList[lampFromListIndex] = it
                        Log.d("collectList","delay test ${lampEntityList.map { lamp ->
//                                    listOf(lamp.id, lamp.selected, lamp.c, lamp.lastConnection)
                                    listOf(lamp.id, lamp.lastConnection)
                                }
                            }"
                        )
                    }

                } else {
                    val lampDomainEntity = mapper.mapLampDtoToEntity(lampDto)

                    val itemFromDb = dipolsDao.getLampItemById(lampDto.id)
                    Log.d("collectList", "itemFromDb = $itemFromDb")
                    if (itemFromDb == null) {
                        val itemToAdd = mapper.mapLampDtoToDbModel(lampDto)
                        dipolsDao.addLampItem(itemToAdd)
                    } else {
                        lampDomainEntity.c = itemFromDb.colorList
                        lampDomainEntity.lampName = itemFromDb.lampName
                    }
                    lampEntityList.add(lampDomainEntity)

                }
                Log.d(
                    "TEST",
                    " = ${lampEntityList.map { it.id to it.lastConnection }}"
                )
            }
        }
    }

    override fun getLatestLampList(): SharedFlow<List<LampDomainEntity>> {
        Log.d("getLatestLampList", " lampListFlow: $lampListFlow")
        return lampListFlow
    }

    override fun selectLamp(lampId: String) {
//        Log.d("onItemClickListener", " lampId: $lampId")
//        Log.d("onItemClickListener", "${lampEntityList.map { it.id }}")
        val oldSelectedItem = lampEntityList.find { lamp -> lamp.selected }
        val oldSelectedItemIndex = lampEntityList.indexOf(oldSelectedItem)
//        Log.d("onItemClickListener", " oldSelectedItem: ${oldSelectedItem?.id}, ${oldSelectedItem?.selected}, ${oldSelectedItem?.c}")

        val newSelectedItem = lampEntityList.find { lamp -> lamp.id == lampId }
        val newSelectedItemIndex = lampEntityList.indexOf(newSelectedItem)
//        Log.d("onItemClickListener", " newSelectedItem: ${newSelectedItem?.id}, ${newSelectedItem?.selected}, ${newSelectedItem?.c}")

        newSelectedItem?.let {
            if (oldSelectedItem?.id != it.id) {
                val oldSelectedItemToUpdate = oldSelectedItem?.copy(selected = false)
//                Log.d("onItemClickListener", " oldSelectedItemToUpdate: ${oldSelectedItemToUpdate?.id}, ${oldSelectedItemToUpdate?.selected}, ${oldSelectedItemToUpdate?.c}")
                oldSelectedItemToUpdate?.let { item ->
                    lampEntityList[oldSelectedItemIndex] = item
//                    Log.d("onItemClickListener", " lampEntityList[oldSelectedItemIndex]: ${lampEntityList[oldSelectedItemIndex].id}, ${lampEntityList[oldSelectedItemIndex].selected}, ${lampEntityList[oldSelectedItemIndex].c}")
                }

                val newSelectedItemToUpdate = it.copy(selected = true)
                lampEntityList[newSelectedItemIndex] = newSelectedItemToUpdate
            }
        }
    }

    override fun unselectLamp() {
        val selectedItem = lampEntityList.find { lamp -> lamp.selected }
        selectedItem?.let {
            val selectedItemIndex = lampEntityList.indexOf(it)
            lampEntityList[selectedItemIndex].selected = false
        }
    }

    override fun changeLocalState(id: String, index: Int, value: Double) {
        Log.d("LampsRepositoryImpl", "changeLocalState $id $index $value")
        val currentLamp = lampEntityList.find { it.id == id }
        currentLamp?.let { lamp ->
            val itemIndex = lampEntityList.indexOf(lamp)

            var colorList = lamp.c.colors.toMutableList()
            if (colorList.isEmpty()) {
                colorList = when (lamp.lampType) {
                    LampType.DIPOL -> mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                    LampType.FIVE_LIGHTS -> mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0)
                    else -> mutableListOf()
                }
            }
            colorList[index] = value
            val changedLamp = lamp.copy(c = ColorList(colorList))
            lampEntityList[itemIndex] = changedLamp

        }
    }


    override fun saveLampToDb(lampDomainEntity: LampDomainEntity) {
        val lampToDb = mapper.mapLampEntityToDbModel(lampDomainEntity)
        dipolsDao.updateLampItem(lampToDb)
    }

    override fun saveLampListToDb() {
        val listToDb = lampEntityList.map { mapper.mapLampEntityToDbModel(it) }
        for (lampToDb in listToDb) {
            dipolsDao.updateLampItem(lampToDb)
        }
    }

    override suspend fun sendColors() {
        var rabbitColorSpeed = 0.5
        while (true) {
//            Log.d("sendColors", "${lampEntityList.map { it.id to it.c }}")

            for (lamp in lampEntityList) {
                val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN))
                var stringToSend = ""

                if (lamp.lampType == LampType.DIPOL) {

                    val r1 = (BigDecimal(lamp.c.colors[0]).setScale(3, RoundingMode.HALF_DOWN))
                    val g1 = (BigDecimal(lamp.c.colors[1]).setScale(3, RoundingMode.HALF_DOWN))
                    val b1 = (BigDecimal(lamp.c.colors[2]).setScale(3, RoundingMode.HALF_DOWN))
                    val r2 = (BigDecimal(lamp.c.colors[3]).setScale(3, RoundingMode.HALF_DOWN))
                    val g2 = (BigDecimal(lamp.c.colors[4]).setScale(3, RoundingMode.HALF_DOWN))
                    val b2 = (BigDecimal(lamp.c.colors[5]).setScale(3, RoundingMode.HALF_DOWN))

                    stringToSend = "r1=$r1;g1=$g1;b1=$b1;r2=$r2;g2=$g2;b2=$b2;rcs=$rcs"

                } else if (lamp.lampType == LampType.FIVE_LIGHTS) {

                    val r = (BigDecimal(lamp.c.colors[0]).setScale(3, RoundingMode.HALF_DOWN))
                    val g = (BigDecimal(lamp.c.colors[1]).setScale(3, RoundingMode.HALF_DOWN))
                    val b = (BigDecimal(lamp.c.colors[2]).setScale(3, RoundingMode.HALF_DOWN))
                    val w = (BigDecimal(lamp.c.colors[3]).setScale(3, RoundingMode.HALF_DOWN))
                    val u = (BigDecimal(lamp.c.colors[4]).setScale(3, RoundingMode.HALF_DOWN))

                    stringToSend = "r=$r;g=$g;b=$b;w=$w;u=$u;rcs=$rcs"
                }
                val address = sender.getInetAddressByName(lamp.ip)
//                Log.d("sendColors", "$stringToSend, $address")
                sender.sendUDPSuspend(stringToSend, address)
            }
            delay(100)
        }
    }

    override fun editLampName(lampId: String, newName: String) {
        Log.d("editLampName", "lampId = $lampId, newName = $newName")
        val lampFromList = lampEntityList.find { lamp -> lamp.id == lampId }
        val lampFromListIndex = lampEntityList.indexOf(lampFromList)
        Log.d("editLampName", "lampFromList = ${lampFromList?.id}, Index = $lampFromListIndex")
        lampEntityList[lampFromListIndex].lampName = newName
        Log.d("editLampName", "lampEntityList[lampFromListIndex].lampName = ${lampEntityList[lampFromListIndex].lampName}")

    }
}