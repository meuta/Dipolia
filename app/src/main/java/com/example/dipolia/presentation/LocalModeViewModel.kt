package com.example.dipolia.presentation

import android.app.Application
import androidx.lifecycle.*
import com.example.dipolia.data.DipoliaRepositoryImpl
import com.example.dipolia.data.LampsRepositoryImpl
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.domain.useCases.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalModeViewModel(application: Application) : AndroidViewModel(application) {

    private val mapper = DipoliaMapper()

    private val repository = DipoliaRepositoryImpl(application)
    private val lampsRepository = LampsRepositoryImpl(application)

    private val sendFollowMeUseCase = SendFollowMeUseCase(lampsRepository)
    private val getLampsUseCase = GetConnectedLampsUseCase(lampsRepository)
    private val getDipolListUseCase = GetDipolListUseCase(lampsRepository)
    private val getFiveLightsUseCase = GetConnectedFiveLightsUseCase(lampsRepository)
    private val selectItemUseCase = SelectLampUseCase(lampsRepository)
    private val unselectLampUseCase = UnselectLampUseCase(lampsRepository)

    private val changeLocalStateUseCase = ChangeLocalStateUseCase(lampsRepository)

    private val receiveLocalModeDataUseCase = ReceiveLocalModeDataUseCase(repository)
    private val testSendLocalModeDataUseCase = TestSendLocalModeDataUseCase(repository)
    private val refreshConnectedListUseCase = RefreshConnectedListUseCase(repository)
    private val getSelectedDipolUseCase = GetSelectedDipolUseCase(repository)
    private val getSelectedLampUseCase = GetSelectedLampUseCase(repository)
    private val dipolsConnectionMonitoringUseCase = DipolsConnectionMonitoringUseCase(repository)
    private val workerStartStopUseCase = WorkerStartStopUseCase(repository)
    private val getIsBroadcastUseCase = GetIsBroadcastUseCase(repository)
    private val getAllLampsTableUseCase = GetAllLampsTableUseCase(repository)
    private val getSelectedConnectedLampTypeUseCase =
        GetSelectedConnectedLampTypeUseCase(repository)

    private val scope = CoroutineScope(Dispatchers.IO)


    val allLampsList = getAllLampsTableUseCase()

    val isBackGroundWork = getIsBroadcastUseCase()

    val myLamps: LiveData<List<LampDomainEntity>> = getLampsUseCase().asLiveData()
    val myDipolsList: LiveData<List<DipolDomainEntity>> = Transformations.map(myLamps) { list ->
        list
            .filter { it.lampType == LampType.DIPOl && it.connected }
            .map { lamp -> mapper.mapLampEntityToDipolEntity(lamp) }
    }
    val myFiveLightList: LiveData<List<FiveLightsDomainEntity>> =
        Transformations.map(myLamps) { list ->
            list
                .filter { it.lampType == LampType.FIVE_LIGHTS && it.connected }
                .map { lamp -> mapper.mapLampEntityToFiveLightsEntity(lamp) }
        }
    val selectedLamp: LiveData<LampDomainEntity> = Transformations.map(myLamps) { list ->
        list.find { it.selected }
    }
    val selectedDipol: LiveData<DipolDomainEntity?> = Transformations.map(selectedLamp) { lamp ->
        lamp?.let {
            if (lamp.lampType == LampType.DIPOl) {
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
//        scope.launch{
//            receiveLocalModeDataUseCase ()
//        }

//        scope.launch{
//            dipolsConnectionMonitoringUseCase ()
//        }
    }


    fun testSendLocalModeData() {
        testSendLocalModeDataUseCase()
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
            refreshConnectedListUseCase()
        }
    }

    fun changeLocalState(set: String, index: Int, componentValue: Double) {
        scope.launch {
            changeLocalStateUseCase(set, index, componentValue)
        }
    }


    fun workerStartStop() {
        workerStartStopUseCase()
    }

}