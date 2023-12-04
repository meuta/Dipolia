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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalModeViewModel @Inject constructor(
    private val collectListUseCase: CollectListUseCase,
    private val getLampsUseCase: GetConnectedLampsUseCase,
    private val selectItemUseCase: SelectLampUseCase,
    private val unselectLampUseCase: UnselectLampUseCase,
    private val changeLocalStateUseCase: ChangeLocalStateUseCase,
    private val saveLampListUseCase: SaveLampListUseCase,
    private val editLampNameUseCase: EditLampNameUseCase,
    private val workManager: WorkManager,
    private val mapper: DipoliaMapper,
    private val setLoopSecondsUseCase: SetLoopSecondsUseCase,
    private val setIsLoopingUseCase: SetIsLoopingUseCase,
    private val getIsLoopingUseCase: GetIsLoopingUseCase,
    private val getLoopSecondsUseCase: GetLoopSecondsUseCase,
) : ViewModel() {

    private val scope = CoroutineScope(Dispatchers.IO)

    val isBackGroundWork = getIsSteaming()

    private fun getIsSteaming(): LiveData<Boolean?> {

        val infoLDManual =
            workManager.getWorkInfosForUniqueWorkLiveData(SendColorListWorker.WORK_NAME)

        return infoLDManual.map {
            Log.d("getIsSteaming", "$it")
            it.isNotEmpty() && it[0].state.toString() == "RUNNING"
        }
    }

    val uiStateFlow = MutableStateFlow(UiState())


    var myLampsSharedFlow: SharedFlow<List<LampDomainEntity>> = getLampsUseCase()

    var myLampsLD: LiveData<List<LampDomainEntity>> = myLampsSharedFlow.asLiveData()

    val myDipolsListLD: LiveData<List<DipolDomainEntity>> = myLampsLD.map { list ->
        list
            .filter { it.lampType == LampType.DIPOL && it.connected }
            .map { lamp -> mapper.mapLampEntityToDipolEntity(lamp) }
    }
    val myFiveLightListLD: LiveData<List<FiveLightsDomainEntity>> =
        myLampsLD.map { list ->
            list
                .filter { it.lampType == LampType.FIVE_LIGHTS && it.connected }
                .map { lamp -> mapper.mapLampEntityToFiveLightsEntity(lamp) }
        }
    val selectedLampLD: LiveData<LampDomainEntity?> = myLampsLD.map() { list ->
        list.find { it.selected }
    }
    val selectedDipolLD: LiveData<DipolDomainEntity?> = selectedLampLD.map { lamp ->
        lamp?.let {
            if (lamp.lampType == LampType.DIPOL) {
                mapper.mapLampEntityToDipolEntity(lamp)
            } else {
                null
            }
        }
    }

    val isLoopingFlow: StateFlow<Boolean> = getIsLoopingUseCase()
    val loopSecondsFlow: StateFlow<Pair<Double, Double>> = getLoopSecondsUseCase()

    val isLoopingLD: LiveData<Boolean?> = isLoopingFlow.asLiveData()
    val loopSecondsLD: LiveData<Pair<Double?, Double?>> = loopSecondsFlow.asLiveData()

    init {
        collectListUseCase()
    }


    fun selectLamp(itemId: String) {
        scope.launch {
            selectItemUseCase(itemId)
        }
    }

    fun editLampName(lampId: String, newName: String) {
        editLampNameUseCase(lampId, newName)
    }

    fun unselectLamp() {
        scope.launch {
            unselectLampUseCase()
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


    fun saveLampList() {
        scope.launch {
            saveLampListUseCase()
        }
    }


    fun updateUiState(uiState: UiState) {
        uiState.isLlLoopSettingsVisible?.let { isLlLoopSettingsVisible ->
            uiStateFlow.update { uiStateFlow.value.copy(isLlLoopSettingsVisible = isLlLoopSettingsVisible) }
        }
    }

    fun setLoopSeconds(secondsChange: Double, secondsStay: Double) {
        scope.launch {
            setLoopSecondsUseCase(secondsChange, secondsStay)
        }
    }

    fun setIsLooping(isLooping: Boolean) {
        scope.launch {
            setIsLoopingUseCase(isLooping)
        }
    }

}