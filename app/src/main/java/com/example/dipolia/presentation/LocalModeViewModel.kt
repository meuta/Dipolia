package com.example.dipolia.presentation

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.workers.SendColorListWorker
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.domain.useCases.ChangeLocalStateUseCase
import com.example.dipolia.domain.useCases.CollectListUseCase
import com.example.dipolia.domain.useCases.EditLampNameUseCase
import com.example.dipolia.domain.useCases.GetConnectedLampsUseCase
import com.example.dipolia.domain.useCases.GetIsLoopingUseCase
import com.example.dipolia.domain.useCases.GetLoopSecondsUseCase
import com.example.dipolia.domain.useCases.SaveLampListUseCase
import com.example.dipolia.domain.useCases.SelectLampUseCase
import com.example.dipolia.domain.useCases.SetIsLoopingUseCase
import com.example.dipolia.domain.useCases.SetLoopSecondsUseCase
import com.example.dipolia.domain.useCases.UnselectLampUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalModeViewModel @Inject constructor(
    collectListUseCase: CollectListUseCase,
    getLampsUseCase: GetConnectedLampsUseCase,
    private val selectItemUseCase: SelectLampUseCase,
    private val unselectLampUseCase: UnselectLampUseCase,
    private val changeLocalStateUseCase: ChangeLocalStateUseCase,
    private val saveLampListUseCase: SaveLampListUseCase,
    private val editLampNameUseCase: EditLampNameUseCase,
    private val workManager: WorkManager,
    private val mapper: DipoliaMapper,
    private val setLoopSecondsUseCase: SetLoopSecondsUseCase,
    private val setIsLoopingUseCase: SetIsLoopingUseCase,
    getIsLoopingUseCase: GetIsLoopingUseCase,
    getLoopSecondsUseCase: GetLoopSecondsUseCase,
) : ViewModel() {

    private val scope = CoroutineScope(Dispatchers.IO)

    val isBackGroundWork = getIsSteaming()

    private fun getIsSteaming(): LiveData<Boolean?> {

        val infoLDManual =
            workManager.getWorkInfosForUniqueWorkLiveData(SendColorListWorker.WORK_NAME)

        return infoLDManual.map {
//            Log.d("getIsSteaming", "$it")
            it.isNotEmpty() && it[0].state.toString() == "RUNNING"
        }
    }

    val uiStateFlow = MutableStateFlow(UiState())

    val isLoopingFlow: StateFlow<Boolean> = getIsLoopingUseCase()
    val loopSecondsFlow: StateFlow<Pair<Double, Double>> = getLoopSecondsUseCase()

    private var myLampsSharedFlow: SharedFlow<List<LampDomainEntity>> = getLampsUseCase()

    var myLampsLD: LiveData<List<LampDomainEntity>> = myLampsSharedFlow.asLiveData()

    val myDipolsListLD: LiveData<List<DipolDomainEntity>> = myLampsLD.map { list ->
        list.asSequence()
            .filter { it.lampType == LampType.DIPOL && it.connected }
            .map { lamp -> mapper.mapLampEntityToDipolEntity(lamp) }
            .toList()
    }

    val myFiveLightListLD: LiveData<List<FiveLightsDomainEntity>> = myLampsLD.map { list ->
        list.asSequence()
            .filter { it.lampType == LampType.FIVE_LIGHTS && it.connected }
            .map { lamp -> mapper.mapLampEntityToFiveLightsEntity(lamp) }
            .toList()
    }


    private var _recyclerViewsDividerVisibilityLD = MutableLiveData<Int>(View.INVISIBLE)
    val recyclerViewsDividerVisibilityLD: LiveData<Int>
        get() = _recyclerViewsDividerVisibilityLD


    private var _pleaseSelectTextViewVisibilityLD = MutableLiveData<Int>(View.INVISIBLE)
    val pleaseSelectTextViewVisibilityLD: LiveData<Int>
        get() = _pleaseSelectTextViewVisibilityLD

    private var _dipolControlLayoutVisibilityLD = MutableLiveData<Int>(View.INVISIBLE)
    val dipolControlLayoutVisibilityLD: LiveData<Int>
        get() = _dipolControlLayoutVisibilityLD

    private var _fiveLightsControlLayoutVisibilityLD = MutableLiveData<Int>(View.INVISIBLE)
    val fiveLightsControlLayoutVisibilityLD: LiveData<Int>
        get() = _fiveLightsControlLayoutVisibilityLD


    private var _selectedDipolLD = MutableLiveData<DipolDomainEntity?>(null)
    val selectedDipolLD: LiveData<DipolDomainEntity?>
        get() = _selectedDipolLD

    private var _selectedFiveLightsLD = MutableLiveData<FiveLightsDomainEntity?>(null)
    val selectedFiveLightsLD: LiveData<FiveLightsDomainEntity?>
        get() = _selectedFiveLightsLD


    private var _selectedDipolColorLabel1LD = MutableLiveData<List<Double>?>(null)
    val selectedDipolColorLabel1LD: LiveData<List<Double>?>
        get() = _selectedDipolColorLabel1LD

    private var _selectedDipolColorLabel2LD = MutableLiveData<List<Double>?>(null)
    val selectedDipolColorLabel2LD: LiveData<List<Double>?>
        get() = _selectedDipolColorLabel2LD

    private var _selectedFiveLightsColorLabelLD = MutableLiveData<List<Double>?>(null)
    val selectedFiveLightsColorLabelLD: LiveData<List<Double>?>
        get() = _selectedFiveLightsColorLabelLD


    init {
        collectListUseCase()
        viewModelScope.launch {
            myLampsSharedFlow.collect { list ->
                val connectedList = list.filter { it.connected }

                val recyclerViewsDividerVisibility =
                    if (connectedList.isNotEmpty()) View.VISIBLE else View.INVISIBLE

                if (_recyclerViewsDividerVisibilityLD.value != recyclerViewsDividerVisibility) {
                    _recyclerViewsDividerVisibilityLD.value = recyclerViewsDividerVisibility
                }

                val selectedLamp = connectedList.find { it.selected }
//                Log.d(TAG, "init: selectedLamp?.lampType = ${selectedLamp?.lampType}")
                when (selectedLamp?.lampType) {

                    LampType.DIPOL -> {
                        if (_dipolControlLayoutVisibilityLD.value == View.INVISIBLE) {
                            _dipolControlLayoutVisibilityLD.value = View.VISIBLE
                        }
                        if (_fiveLightsControlLayoutVisibilityLD.value == View.VISIBLE) {
                            _fiveLightsControlLayoutVisibilityLD.value = View.INVISIBLE
                        }
                        if (_pleaseSelectTextViewVisibilityLD.value == View.VISIBLE) {
                            _pleaseSelectTextViewVisibilityLD.value = View.INVISIBLE
                        }

                        _selectedFiveLightsLD.value?.let { _selectedFiveLightsLD.value = null }

                        if (_selectedDipolLD.value?.id != selectedLamp.id) {
                            _selectedDipolLD.value = mapper.mapLampEntityToDipolEntity(selectedLamp)
                        }

                        if (_selectedDipolColorLabel1LD.value != selectedLamp.c.colors.take(3)) {
                            _selectedDipolColorLabel1LD.value = selectedLamp.c.colors.take(3)
                        }
                        if (_selectedDipolColorLabel2LD.value != selectedLamp.c.colors.takeLast(3)) {
                            _selectedDipolColorLabel2LD.value = selectedLamp.c.colors.takeLast(3)
                        }
                    }

                    LampType.FIVE_LIGHTS -> {
                        if (_fiveLightsControlLayoutVisibilityLD.value == View.INVISIBLE) {
                            _fiveLightsControlLayoutVisibilityLD.value = View.VISIBLE
                        }
                        if (_dipolControlLayoutVisibilityLD.value == View.VISIBLE) {
                            _dipolControlLayoutVisibilityLD.value = View.INVISIBLE
                        }
                        if (_pleaseSelectTextViewVisibilityLD.value == View.VISIBLE) {
                            _pleaseSelectTextViewVisibilityLD.value = View.INVISIBLE
                        }

                        _selectedDipolLD.value?.let { _selectedDipolLD.value = null }

                        if (_selectedFiveLightsLD.value?.id != selectedLamp.id) {
                            _selectedFiveLightsLD.value =
                                mapper.mapLampEntityToFiveLightsEntity(selectedLamp)
                        }

                        if (_selectedFiveLightsColorLabelLD.value != selectedLamp.c.colors) {
                            _selectedFiveLightsColorLabelLD.value = selectedLamp.c.colors
                        }

                    }

                    else -> {
                        if (_dipolControlLayoutVisibilityLD.value == View.VISIBLE) {
                            _dipolControlLayoutVisibilityLD.value = View.INVISIBLE
                        }
                        if (_fiveLightsControlLayoutVisibilityLD.value == View.VISIBLE) {
                            _fiveLightsControlLayoutVisibilityLD.value = View.INVISIBLE
                        }

                        val pleaseSelectTextViewVisibility =
                            if (connectedList.isEmpty()) View.INVISIBLE else View.VISIBLE

                        if (_pleaseSelectTextViewVisibilityLD.value != pleaseSelectTextViewVisibility) {
                            _pleaseSelectTextViewVisibilityLD.value = pleaseSelectTextViewVisibility
                        }

                        _selectedDipolLD.value?.let { _selectedDipolLD.value = null }
                        _selectedFiveLightsLD.value?.let { _selectedFiveLightsLD.value = null }
                    }
                }

            }
        }
    }

    fun selectLamp(itemId: String) {
//        Log.d(TAG, "selectLamp: itemId = $itemId")
        selectItemUseCase(itemId)
    }

    fun editLampName(lampId: String, newName: String) {
        editLampNameUseCase(lampId, newName)
    }

    fun unselectLamp() {
        unselectLampUseCase()
    }

    fun changeLocalState(id: String, index: Int, value: Int) {
        changeLocalStateUseCase(id, index, value / 100.0)
    }


    fun workerStartStop() {

        val workInfoLF = workManager.getWorkInfosForUniqueWork(SendColorListWorker.WORK_NAME)
//        Log.d("onClick workerStartStop", "WORK_NAME ${SendColorListWorker.WORK_NAME}")

        val workInfo = workInfoLF.get()
//        if (workInfo.isNotEmpty()) {
//            val workState = workInfo[0].state.toString()
//            Log.d("onClick workerStartStop", "workerState = $workState")
//        }

        if (workInfo.isNotEmpty() && workInfo[0].state.toString() == "RUNNING") {
//            Log.d("onClick workerStartStop", "RUNNING")
            workManager.cancelUniqueWork(SendColorListWorker.WORK_NAME)
        } else {
//            Log.d("onClick workerStartStop", "NOT RUNNING")
            workManager.enqueueUniqueWork(
                SendColorListWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,  //what to do, if another worker will be started
                SendColorListWorker.makeRequest()
            )
        }
    }


    fun saveLampList() {
        scope.launch {
            saveLampListUseCase()
        }
    }


    fun updateUiState(uiState: UiState) {
        uiStateFlow.update { uiStateFlow.value.copy(
            isLlLoopSettingsVisible = uiState.isLlLoopSettingsVisible,
            doNotUpdateETSecondsChange = uiState.doNotUpdateETSecondsChange,
            doNotUpdateETSecondsStay = uiState.doNotUpdateETSecondsStay
        )}
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

    companion object {

        private const val TAG = "LocalModeViewModel"
    }
}