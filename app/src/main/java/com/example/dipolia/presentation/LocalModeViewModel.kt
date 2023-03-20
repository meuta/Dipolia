package com.example.dipolia.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.dipolia.data.DipoliaRepositoryImpl
import com.example.dipolia.data.LampsRepositoryImpl
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.useCases.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LocalModeViewModel(application: Application) : AndroidViewModel(application) {

    private val mapper = DipoliaMapper()

    private val repository = DipoliaRepositoryImpl(application)
    private val lampsRepository = LampsRepositoryImpl(application)
    private val receiveLocalModeDataUseCase = ReceiveLocalModeDataUseCase(repository)
    private val sendFollowMeUseCase = SendFollowMeUseCase(repository)
    private val testSendLocalModeDataUseCase = TestSendLocalModeDataUseCase(repository)
    private val getDipolListUseCase = GetDipolListUseCase(repository)
    private val selectItemUseCase = SelectItemUseCase(repository)
    private val refreshConnectedListUseCase = RefreshConnectedListUseCase(repository)
    private val changeLocalStateUseCase = ChangeLocalStateUseCase(repository)
    private val getSelectedDipolUseCase = GetSelectedDipolUseCase(repository)
    private val getSelectedLampUseCase = GetSelectedLampUseCase(repository)
    private val dipolsConnectionMonitoringUseCase = DipolsConnectionMonitoringUseCase(repository)
    private val workerStartStopUseCase = WorkerStartStopUseCase(repository)
    private val getIsBroadcastUseCase = GetIsBroadcastUseCase(repository)
    private val getFiveLightsUseCase = GetConnectedFiveLightsUseCase(repository)
    private val getAllLampsTableUseCase = GetAllLampsTableUseCase(repository)
    private val unselectLampUseCase = UnselectLampUseCase(repository)
    private val getSelectedConnectedLampTypeUseCase =
        GetSelectedConnectedLampTypeUseCase(repository)

    private val scope = CoroutineScope(Dispatchers.IO)


//    var dipolList = getDipolListUseCase()


    val allLampsList = getAllLampsTableUseCase()

    val fiveLights = getFiveLightsUseCase()
    val selectedDipol = getSelectedDipolUseCase()
    val selectedConnectedLampType = getSelectedConnectedLampTypeUseCase()

    //    val selectedLamp = getSelectedLampUseCase()
    val isBackGroundWork = getIsBroadcastUseCase()

    val myDipolsList: LiveData<List<DipolDomainEntity>> = lampsRepository
        .latestDipolLampDomainEntityList()
        .asLiveData()

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

    fun selectItem(itemId: String) {
        scope.launch {
            selectItemUseCase(itemId)
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

    fun unselectLamp() {
        scope.launch {
            unselectLampUseCase()
        }
    }

    fun workerStartStop() {
        workerStartStopUseCase()
    }

}