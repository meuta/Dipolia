package com.example.dipolia.presentation

import android.util.Log
import androidx.lifecycle.*
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.workers.SendColorListWorker
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.domain.useCases.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocalModeViewModel @Inject constructor(
    private val sendFollowMeUseCase: SendFollowMeUseCase,
    private val getLampsUseCase: GetConnectedLampsUseCase,
    private val selectItemUseCase: SelectLampUseCase,
    private val unselectLampUseCase: UnselectLampUseCase,
    private val changeLocalStateUseCase: ChangeLocalStateUseCase,
    private val saveLampUseCase: SaveLampUseCase,
    private val saveLampListUseCase: SaveLampListUseCase,
    private val workManager: WorkManager,
    private val mapper: DipoliaMapper
) : ViewModel() {



//    private val testSendLocalModeDataUseCase = TestSendLocalModeDataUseCase(repository)
//    private val refreshConnectedListUseCase = RefreshConnectedListUseCase(repository)
//    private val getSelectedLampUseCase = GetSelectedLampUseCase(repository)
//    private val getAllLampsTableUseCase = GetAllLampsTableUseCase(repository)


    private val scope = CoroutineScope(Dispatchers.IO)


//    val allLampList = getAllLampsTableUseCase()

    val isBackGroundWork = getIsBroadcast()
    private fun getIsBroadcast(): LiveData<Boolean?> {

        val infoLD = workManager.getWorkInfosForUniqueWorkLiveData(SendColorListWorker.WORK_NAME)
        Log.d("getIsBroadcast", "infoLD = $infoLD")
        return Transformations.map(infoLD) {
            Log.d("getIsBroadcast", "$it")
            it.isNotEmpty() && it[0].state.toString() == "RUNNING"
        }
    }

//    fun getLamps() {
//        scope.launch {
//            getLampsUseCase()
//        }
//    }
    var myLamps = getLampsUseCase().asLiveData()


    var mySelLamp: LampDomainEntity? = null


    val myDipolsList: LiveData<List<DipolDomainEntity>> = Transformations.map(myLamps) { list ->
        list
            .filter { it.lampType == LampType.DIPOL && it.connected }
            .map { lamp -> mapper.mapLampEntityToDipolEntity(lamp) }
    }
    val myFiveLightList: LiveData<List<FiveLightsDomainEntity>> =
        Transformations.map(myLamps) { list ->
            list
                .filter { it.lampType == LampType.FIVE_LIGHTS && it.connected }
                .map { lamp -> mapper.mapLampEntityToFiveLightsEntity(lamp) }
        }
    val selectedLamp: LiveData<LampDomainEntity> = Transformations.map(myLamps) { list ->
        mySelLamp = list.find { it.selected }
//        list.find { it.selected }
        mySelLamp
    }
    val selectedDipol: LiveData<DipolDomainEntity?> = Transformations.map(selectedLamp) { lamp ->
        lamp?.let {
            if (lamp.lampType == LampType.DIPOL) {
                mapper.mapLampEntityToDipolEntity(lamp)
            } else {
                null
            }
        }
    }

    init {  //This code will be executes every time automatically with creating of this object
        scope.launch {
            sendFollowMeUseCase()
        }
//        scope.launch {
//            myLamps = getLampsUseCase().asLiveData()
//        }
//        scope.launch {
//            // Trigger the flow and consume its elements using collect
////            val case = getLampsUseCase()
//            getLampsUseCase().collect { lamps ->
////            lampsRepository.latestDipolLampDtoList.collect { dipols ->
//                // Update View
//                Log.d("TEST_", "LampDomainEntityList = ${lamps.map { it.id to it.lastConnection}}")
//            }
//        }
    }


    fun testSendLocalModeData() {
//        testSendLocalModeDataUseCase()
    }

    fun selectLamp(itemId: String) {
        scope.launch {
            selectItemUseCase(itemId)
        }
    }

    fun unselectLamp() {
        scope.launch {
            unselectLampUseCase()
        }
    }

    fun refreshConnectedList() {
        scope.launch {
//            refreshConnectedListUseCase()
        }
    }

    fun changeLocalState(set: String, index: Int, componentValue: Double) {
        scope.launch {
            changeLocalStateUseCase(set, index, componentValue)
        }
    }


    fun workerStartStop() {

        val workInfoLF = workManager.getWorkInfosForUniqueWork(SendColorListWorker.WORK_NAME)
//        Log.d("onClick workerStartStop", "WORK_NAME ${SendColorListWorker.WORK_NAME}")

        val workInfo = workInfoLF.get()
        if (workInfo.isNotEmpty()) {
            val workState = workInfo[0].state.toString()
            Log.d("onClick workerStartStop", "workerState = $workState")
        }
//
        if (workInfo.isNotEmpty() && workInfo[0].state.toString() == "RUNNING") {
            Log.d("onClick workerStartStop", "workerState == \"RUNNING\"")
            workManager.cancelAllWork()
        } else {
            Log.d("onClick workerStartStop", "workerState == \"CANCELED\"")
//            val data = Data.Builder()
//                .putString(SendColorListWorker.IP, "")
//                .putDoubleArray(SendColorListWorker.LIST, doubleArrayOf(0.0))
//                .build()
            mySelLamp?.let {
                val lampType = when(it.lampType){
                    LampType.DIPOL -> "dipol"
                    LampType.FIVE_LIGHTS -> "fiveLights"
                    else -> "unknown"
                }
                workManager.enqueueUniqueWork(
                SendColorListWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,  //what to do, if another worker will be started
//                SendColorListWorker.makeRequest(myLamps)
                SendColorListWorker.makeRequest(it.ip, lampType, it.c.colors)
//                SendColorListWorker.makeRequest()
                )
            }
        }
    }

    fun saveLamp(lampDomainEntity: LampDomainEntity){
        scope.launch {
            saveLampUseCase(lampDomainEntity)
        }
    }

    fun saveLampList(list: List<LampDomainEntity>){
        scope.launch {
            saveLampListUseCase(list)
        }
    }


}