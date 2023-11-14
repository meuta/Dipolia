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

            Log.d(
                "TEST",
                "latestLampList = ${
                    latestLampList.map {
                        listOf(
                            it.id,
                            it.lampName,
                            it.lastConnection
                        )
                    }
                }"
            )
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
                    lampEntityList.withIndex().find { lamp -> lamp.value.id == lampDto.id }?.let {
                        it.value.lastConnection = lampDto.lastConnection
                        lampEntityList[it.index] = it.value
                        Log.d(
                            "collectList", "delay test ${
                                lampEntityList.map { lamp ->
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
        val oldSelectedItemWithIndex = lampEntityList.withIndex().find { lamp -> lamp.value.selected }
        lampEntityList.withIndex().find { lamp -> lamp.value.id == lampId }?.let {
            if (oldSelectedItemWithIndex?.value?.id != it.value.id) {
                val oldSelectedItemToUpdate =
                    oldSelectedItemWithIndex?.value?.copy(selected = false)
                oldSelectedItemToUpdate?.let { item ->
                    lampEntityList[oldSelectedItemWithIndex.index] = item
                }
                val newSelectedItemToUpdate = it.value.copy(selected = true)
                lampEntityList[it.index] = newSelectedItemToUpdate
            }
        }
    }

    override fun unselectLamp() {
        lampEntityList.withIndex().find { lamp -> lamp.value.selected }?.index?.let {
            lampEntityList[it].selected = false
        }
    }

    override fun changeLocalState(id: String, index: Int, value: Double) {
        Log.d("LampsRepositoryImpl", "changeLocalState $id $index $value")

        lampEntityList.withIndex().find { lamp -> lamp.value.id == id }?.let {
            var colorList = it.value.c.colors.toMutableList()
            if (colorList.isEmpty()) {
                colorList = when (it.value.lampType) {
                    LampType.DIPOL -> mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                    LampType.FIVE_LIGHTS -> mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0)
                    else -> mutableListOf()
                }
            }
            colorList[index] = value
            val changedLamp = it.value.copy(c = ColorList(colorList))
            lampEntityList[it.index] = changedLamp
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
        lampEntityList.withIndex().find { lamp -> lamp.value.id == lampId }?.index?.let {
            lampEntityList[it].lampName = newName
        }
    }
}