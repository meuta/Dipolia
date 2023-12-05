package com.example.dipolia.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.dipolia.data.database.ColorList
import com.example.dipolia.data.database.DipolsDao
import com.example.dipolia.data.datastore.StreamingPreferences
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
import kotlinx.coroutines.launch
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LampsRepositoryImpl @Inject constructor(
    private val dipolsDao: DipolsDao,
    private val mapper: DipoliaMapper,
    private val lampsRemoteDataSource: LampsRemoteDataSource,
    private val sender: UDPClient,
    private val streamingPreferences: DataStore<Preferences>
) : LampsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
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
    }.shareIn(scope, SharingStarted.Lazily)



    override fun collectList() {
        scope.launch {
            while (true) {
                sender.sendUDPSuspend("Follow me")
                Log.d("LampsRepositoryImpl", "sendUDPSuspend(Follow me)")
                delay(1000)
            }
        }
        scope.launch {
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
    }

    override fun getLatestLampList(): SharedFlow<List<LampDomainEntity>> {
        Log.d("getLatestLampList", " lampListFlow: $lampListFlow")
        return lampListFlow
    }


    override fun selectLamp(lampId: String) {
        val oldSelectedItemWithIndex =
            lampEntityList.withIndex().find { lamp -> lamp.value.selected }
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

                val tints = lamp.c.colors.map { BigDecimal(it).setScale(3, RoundingMode.HALF_DOWN) }

                if (lamp.lampType == LampType.DIPOL) {

                    stringToSend = "r1=${tints[0]};g1=${tints[1]};b1=${tints[2]};r2=${tints[3]};g2=${tints[4]};b2=${tints[5]};rcs=$rcs"

                } else if (lamp.lampType == LampType.FIVE_LIGHTS) {

                    stringToSend = "r=${tints[0]};g=${tints[1]};b=${tints[2]};w=${tints[3]};u=${tints[4]}};rcs=$rcs"

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


    private val isLoopingFlow: StateFlow<Boolean> = streamingPreferences.data
        .catch { exception ->
            /*
                 * dataStore.data throws an IOException when an error
                 * is encountered when reading data
                 */
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            // Get our name value, defaulting to "" if not set
            preferences[KEY_IS_LOOPING] ?: false
        }.stateIn(scope, SharingStarted.Eagerly, false)

    private val loopSecondsFlow: StateFlow<Pair<Double, Double>> = streamingPreferences.data
        .catch { exception ->
            /*
                 * dataStore.data throws an IOException when an error
                 * is encountered when reading data
                 */
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            // Get our name value, defaulting to "" if not set
            (preferences[KEY_SECONDS_CHANGE] ?: 0.0) to (preferences[KEY_SECONDS_STAY] ?: 0.0)
        }.stateIn(scope, SharingStarted.Eagerly, 0.0 to 0.0)

    override suspend fun setLoopSeconds(secondsChange: Double, secondsStay: Double){
        streamingPreferences.edit { preferences ->
            preferences[KEY_SECONDS_CHANGE] = secondsChange
            preferences[KEY_SECONDS_STAY] = secondsStay
        }
    }

    override suspend fun setIsLooping(isLooping: Boolean){
        streamingPreferences.edit { preferences ->
            preferences[KEY_IS_LOOPING] = isLooping
        }
    }


    override fun getIsLooping(): StateFlow<Boolean> {
        return isLoopingFlow
    }

    override fun getLoopSeconds(): StateFlow<Pair<Double, Double>> {
        return loopSecondsFlow
    }


    suspend fun fetchInitialPreferences() =
        mapLoopPreferences(streamingPreferences.data.first().toPreferences())

    private fun mapLoopPreferences(preferences: Preferences): StreamingPreferences {
        val secondsChange = preferences[KEY_SECONDS_CHANGE] ?: 0.0
        val secondsStay = preferences[KEY_SECONDS_STAY] ?: 0.0
        val isLooping = preferences[KEY_IS_LOOPING] ?: false
        return StreamingPreferences(secondsChange, secondsStay, isLooping)
    }

    private companion object {

        val KEY_SECONDS_CHANGE = doublePreferencesKey(name = "secondsChange")
        val KEY_SECONDS_STAY = doublePreferencesKey(name = "secondsStay")
        val KEY_IS_LOOPING = booleanPreferencesKey(name = "isLooping")

    }
}