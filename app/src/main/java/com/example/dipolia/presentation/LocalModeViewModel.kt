package com.example.dipolia.presentation

import android.util.Log
import androidx.lifecycle.*
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocalModeViewModel @Inject constructor(
    private val sendFollowMeUseCase: SendFollowMeUseCase,
    private val collectListUseCase: CollectListUseCase,
    private val getLampsUseCase: GetConnectedLampsUseCase,
    private val selectItemUseCase: SelectLampUseCase,
    private val unselectLampUseCase: UnselectLampUseCase,
    private val changeLocalStateUseCase: ChangeLocalStateUseCase,
    private val saveLampUseCase: SaveLampUseCase,
    private val saveLampListUseCase: SaveLampListUseCase,
    private val workManager: WorkManager,
    private val mapper: DipoliaMapper,
//    private val sendColorsUseCase: SendColorsUseCase
) : ViewModel() {


    private val scope = CoroutineScope(Dispatchers.IO)


    val isBackGroundWork = getIsSteaming()
    private fun getIsSteaming(): LiveData<Boolean?> {

        val infoLD = workManager.getWorkInfosForUniqueWorkLiveData(SendColorListWorker.WORK_NAME)
        Log.d("getIsSteaming", "infoLD = $infoLD")
        return Transformations.map(infoLD) {
            Log.d("getIsSteaming", "$it")
            it.isNotEmpty() && it[0].state.toString() == "RUNNING"
        }
    }

    var myLampsSharedFlow: SharedFlow<List<LampDomainEntity>> = getLampsUseCase()

    var myLampsLD: LiveData<List<LampDomainEntity>> = myLampsSharedFlow.asLiveData()

    val myDipolsListLD: LiveData<List<DipolDomainEntity>> = Transformations.map(myLampsLD) { list ->
        list
            .filter { it.lampType == LampType.DIPOL && it.connected }
            .map { lamp -> mapper.mapLampEntityToDipolEntity(lamp) }
    }
    val myFiveLightListLD: LiveData<List<FiveLightsDomainEntity>> =
        Transformations.map(myLampsLD) { list ->
            list
                .filter { it.lampType == LampType.FIVE_LIGHTS && it.connected }
                .map { lamp -> mapper.mapLampEntityToFiveLightsEntity(lamp) }
        }
    val selectedLampLD: LiveData<LampDomainEntity> = Transformations.map(myLampsLD) { list ->
        list.find { it.selected }
    }
    val selectedDipolLD: LiveData<DipolDomainEntity?> = Transformations.map(selectedLampLD) { lamp ->
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
        scope.launch {
            collectListUseCase()
        }
//        scope.launch {
//            repository.getLatestLampList().collectLatest { lamps ->
//                Log.d("TEST_ViewModel", "LampDomainEntityList = ${lamps.map { it.id to it.lastConnection }}")
//            }
//        }

        scope.launch {
//            sendColorsUseCase()
        }

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

    fun changeLocalState(id: String, index: Int, value: Int) {
            changeLocalStateUseCase(id, index, value / 100.0)
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
            Log.d("onClick workerStartStop", "RUNNING")
            workManager.cancelAllWork()
        } else {
            Log.d("onClick workerStartStop", "NOT RUNNING")

            scope.launch {

                workManager.enqueueUniqueWork(
                    SendColorListWorker.WORK_NAME,
                    ExistingWorkPolicy.REPLACE,  //what to do, if another worker will be started
                    SendColorListWorker.makeRequest()
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