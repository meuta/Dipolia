package com.example.dipolia.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.dipolia.data.DipoliaRepositoryImpl
import com.example.dipolia.domain.useCases.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalModeViewModel(application: Application): AndroidViewModel(application) {

    private val repository = DipoliaRepositoryImpl(application)
    private val receiveLocalModeDataUseCase = ReceiveLocalModeDataUseCase(repository)
    private val sendFollowMeUseCase = SendFollowMeUseCase(repository)
//    private val testSendLocalModeDataUseCase = TestSendLocalModeDataUseCase(repository)
    private val getDipolListUseCase = GetDipolListUseCase(repository)
    private val selectDipolUseCase = SelectDipolUseCase(repository)
    private val refreshConnectedListUseCase = RefreshConnectedListUseCase(repository)
    private val changeLocalStateUseCase = ChangeLocalStateUseCase(repository)
    private val getSelectedDipolUseCase = GetSelectedDipolUseCase(repository)
    private val unselectDipolUseCase = UnselectDipolUseCase(repository)
    private val dipolsConnectionMonitoringUseCase = DipolsConnectionMonitoringUseCase(repository)
    private val workerStartStopUseCase = WorkerStartStopUseCase(repository)
    private val getIsBroadcastUseCase = GetIsBroadcastUseCase(repository)
    private val getFiveLightsUseCase = GetFiveLightsUseCase(repository)
    private val getAllLampsTableUseCase = GetAllLampsTableUseCase(repository)

    private val scope = CoroutineScope(Dispatchers.IO)

    val dipolList = getDipolListUseCase()
    val allLampsList = getAllLampsTableUseCase()
    val fiveLights = getFiveLightsUseCase()
    val selectedDipol = getSelectedDipolUseCase()
    val isBackGroundWork = getIsBroadcastUseCase()
    init {  //This code will be executes every time automatically with creating of this object
        scope.launch{
            sendFollowMeUseCase()
        }
        scope.launch{
            receiveLocalModeDataUseCase ()
        }
//        scope.launch{
//            dipolsConnectionMonitoringUseCase ()
//        }
    }

//    fun testSendLocalModeData() {
//            testSendLocalModeDataUseCase()
//    }

    fun changeSelectedDipol(dipolId: String){
        scope.launch {
            selectDipolUseCase(dipolId)
        }
    }

    fun selectFiveLights(fiveLightsId: String){
        scope.launch {
            selectDipolUseCase(fiveLightsId)
        }
    }


    fun refreshConnectedList() {
        scope.launch {
            refreshConnectedListUseCase()
        }
    }

//    fun changeLocalState(
//        dipolItem: DipolDomainEntity,
//        horn: Horn,
//        component: ColorComponent,
//        componentDiff: Double
//    ) {
    fun changeLocalState(set: String, index: Int, componentValue: Double){
        scope.launch {
            changeLocalStateUseCase(set, index, componentValue)
        }
    }

    fun unselectDipol(){
        scope.launch {
            unselectDipolUseCase()
        }
    }

    fun workerStartStop() {
        workerStartStopUseCase()
    }

}