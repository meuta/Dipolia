package com.example.dipolia.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.dipolia.data.DipoliaRepositoryImpl
import com.example.dipolia.domain.ColorComponent
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.Horn
import com.example.dipolia.domain.useCases.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocalModeViewModel(application: Application): AndroidViewModel(application) {

    private val repository = DipoliaRepositoryImpl(application)
    private val receiveLocalModeDataUseCase = ReceiveLocalModeDataUseCase(repository)
    private val sendFollowMeUseCase = SendFollowMeUseCase(repository)
    private val testSendLocalModeDataUseCase = TestSendLocalModeDataUseCase(repository)
    private val getDipolListUseCase = GetDipolListUseCase(repository)
    private val selectDipolUseCase = SelectDipolUseCase(repository)
    private val refreshConnectedListUseCase = RefreshConnectedListUseCase(repository)
    private val changeLocalStateUseCase = ChangeLocalStateUseCase(repository)
    private val getSelectedDipolUseCase = GetSelectedDipolUseCase(repository)
    private val unselectDipolUseCase = UnselectDipolUseCase(repository)

    private val scope = CoroutineScope(Dispatchers.IO)

    val dipolList = getDipolListUseCase()
    val selectedDipol = getSelectedDipolUseCase()
    init {  //This code will be executes every time automatically with creating of this object
        scope.launch{
            sendFollowMeUseCase()
        }
        scope.launch{
            receiveLocalModeDataUseCase ()
        }
    }

    fun testSendLocalModeData() {
            testSendLocalModeDataUseCase()
    }

    fun changeSelectedDipol(dipolId: String){
        scope.launch {
            selectDipolUseCase(dipolId)
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
    fun changeLocalState(index: Int, componentValue: Double){
        scope.launch {
            changeLocalStateUseCase(index, componentValue)
        }
    }

    fun unselectDipol(){
        scope.launch {
            unselectDipolUseCase()
        }
    }


}