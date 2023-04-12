package com.example.dipolia.data

import android.app.Application
import android.util.Log

import com.example.dipolia.data.database.ColorList
import com.example.dipolia.data.database.DipolsDao
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.network.LampsRemoteDataSource
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.domain.LampsRepository
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class LampsRepositoryImpl @Inject constructor(
    private val dipolsDao: DipolsDao,
    private val mapper: DipoliaMapper,
    private val lampsRemoteDataSource: LampsRemoteDataSource,
    private val sender: UDPClient//,
//    private val application: Application
) : LampsRepository {


    val lampEntityList = mutableListOf<LampDomainEntity>()
    lateinit var lampEntityListSharedFlow: SharedFlow<List<LampDomainEntity>>
    var selectedLamp: LampDomainEntity? = null
//    var selectedLamp: LampDomainEntity? = LampDomainEntity("", "", LampType.UNKNOWN_LAMP_TYPE, ColorList(listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)) )

    override suspend fun sendFollowMe() {
        while (true) {
            sender.sendUDPSuspend("Follow me")
            delay(100)
        }
    }

    override fun getLatestLampList(): Flow<List<LampDomainEntity>> = lampsRemoteDataSource.myLampDto
        .map { lampDto ->
            var already = 0

            if (lampDto.id in lampEntityList.map { it.id }) {
                val lampFromList = lampEntityList.find { lamp -> lamp.id == lampDto.id }
                val lampFromListIndex = lampEntityList.indexOf(lampFromList)
                lampFromList?.let {
                    it.lastConnection = lampDto.lastConnection
                    it.selected = (selectedLamp?.id == it.id)

                    lampEntityList[lampFromListIndex] = it
                    Log.d(
                        "getLatestLampList",
                        "${lampEntityList.map { lamp -> lamp.id to lamp.connected }}"
                    )
                }
                already = 1
            }

            if (already == 0) {
                val lampDomainEntity = mapper.mapLampDtoToEntity(lampDto)

                val itemFromDb = dipolsDao.getLampItemById(lampDto.id)
                Log.d("UDP receiveLocalModeData", "itemFromDb = $itemFromDb")
                if (itemFromDb == null) {
                    val itemToAdd = mapper.mapLampDtoToDbModel(lampDto)
                    dipolsDao.addLampItem(itemToAdd)
                } else {
                    lampDomainEntity.c = itemFromDb.colorList
//                        Log.d("TEST", "exists")
                }
                lampEntityList.add(lampDomainEntity)
//                Log.d(
//                    "TEST",
//                    "lampEntityList = ${lampEntityList.map { item -> item.id to item.lastConnection }}"
//                )
            }

            lampEntityList
        }.flowOn(Dispatchers.IO)


    override fun selectLamp(lampId: String) {
//        Log.d("onItemClickListener", " lampId: $lampId")
//        Log.d("onItemClickListener", "${lampEntityList.map { it.id }}")
        val oldSelectedItem = lampEntityList.find { lamp -> lamp.selected }
        val oldSelectedItemIndex = lampEntityList.indexOf(oldSelectedItem)
//        Log.d("onItemClickListener", " oldSelectedItem: ${oldSelectedItem?.id}, ${oldSelectedItem?.selected}")

        val newSelectedItem = lampEntityList.find { lamp -> lamp.id == lampId }
        val newSelectedItemIndex = lampEntityList.indexOf(newSelectedItem)
//        Log.d("onItemClickListener", " newSelectedItem: ${newSelectedItem?.id}, ${newSelectedItem?.selected}")

        newSelectedItem?.let {
            if (oldSelectedItem?.id != it.id) {
                val oldSelectedItemToUpdate = oldSelectedItem?.copy(selected = false)

                oldSelectedItemToUpdate?.let { item ->
                    lampEntityList[oldSelectedItemIndex] = item
                }
                val newSelectedItemToUpdate = it.copy(selected = true)
                lampEntityList[newSelectedItemIndex] = newSelectedItemToUpdate
                selectedLamp = newSelectedItemToUpdate
//                Log.d(
//                    "onItemClickListener",
//                    " selectedLamp: ${selectedLamp?.id} ${selectedLamp?.selected}"
//                )
            }
        }
    }

    override fun unselectLamp() {
        val selectedItem = lampEntityList.find { lamp -> lamp.selected }
        selectedItem?.let {
            val selectedItemIndex = lampEntityList.indexOf(it)
            selectedLamp = null
            lampEntityList[selectedItemIndex].selected = false
        }
    }

    override fun changeLocalState(set: String, index: Int, value: Double) {
        Log.d("LampsRepositoryImpl", "changeLocalState $set $index $value")
        selectedLamp?.let { lamp ->
            Log.d("LampsRepositoryImpl", "changeLocalState ${lamp.id}")

            var colorList = lamp.c.colors.toMutableList()
            Log.d("changeLocalState", "colorList $colorList")

            if (colorList.isEmpty()) {
                colorList = when (set) {
                    "dipol" -> mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                    "fiveLights" -> mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0)
                    else -> mutableListOf()
                }
            }
            colorList[index] = value
            val newLampItem = lamp.copy(c = ColorList(colorList))
            Log.d("changeLocalState", "newLampItem $newLampItem")

            selectedLamp = newLampItem
            Log.d("changeLocalState", "selectedLamp $selectedLamp")

            selectedLamp?.let {
                val item = lampEntityList.find { lamp -> lamp.id == it.id }
                val itemIndex = lampEntityList.indexOf(item)
                val changedItem = item?.copy(c = ColorList(colorList))
                changedItem?.let { cItem ->
                    lampEntityList[itemIndex] = cItem
                }
            }
        }
    }


    override fun saveLampToDb(lampDomainEntity: LampDomainEntity) {
        val lampToDb = mapper.mapLampEntityToDbModel(lampDomainEntity)
        dipolsDao.updateLampItem(lampToDb)
    }

    override fun saveLampListToDb(list: List<LampDomainEntity>) {
        val listToDb = list.map { mapper.mapLampEntityToDbModel(it) }
        for (lampToDb in listToDb) {
            dipolsDao.updateLampItem(lampToDb)
        }
    }

    override suspend fun sendColors() {
        var rabbitColorSpeed = 0.5
        while (true) {
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
                sender.sendUDPSuspend(stringToSend, address)
            }
            delay(100)
        }
    }



}