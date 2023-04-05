package com.example.dipolia.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.dipolia.data.DipoliaRepositoryImpl
import com.example.dipolia.data.LampsRepositoryImpl
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.workers.SendColorListWorker
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.domain.useCases.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocalModeViewModel @Inject constructor(
    private val sendFollowMeUseCase: SendFollowMeUseCase,
    private val getLampsUseCase: GetConnectedLampsUseCase,
    private val selectItemUseCase: SelectLampUseCase,
    private val unselectLampUseCase: UnselectLampUseCase,
    private val changeLocalStateUseCase: ChangeLocalStateUseCase,
    private val workManager: WorkManager
) : ViewModel() {

    private val mapper = DipoliaMapper()

//    private val workManager = WorkManager.getInstance(application)


//    private val testSendLocalModeDataUseCase = TestSendLocalModeDataUseCase(repository)
//    private val refreshConnectedListUseCase = RefreshConnectedListUseCase(repository)
//    private val getSelectedLampUseCase = GetSelectedLampUseCase(repository)
//    private val getAllLampsTableUseCase = GetAllLampsTableUseCase(repository)


    private val scope = CoroutineScope(Dispatchers.IO)


//    val allLampsList = getAllLampsTableUseCase()

    val isBackGroundWork = getIsBroadcast()
    private fun getIsBroadcast(): LiveData<Boolean?> {

        val infoLD = workManager.getWorkInfosForUniqueWorkLiveData(SendColorListWorker.WORK_NAME)
        Log.d("getIsBroadcast", "infoLD = $infoLD")
        return Transformations.map(infoLD) {
            Log.d("getIsBroadcast", "$it")
            it.isNotEmpty() && it[0].state.toString() == "RUNNING"
        }
    }

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
            workManager.enqueueUniqueWork(
                SendColorListWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,  //what to do, if another worker will be started
//                SendColorListWorker.makeRequest(myLamps)
                SendColorListWorker.makeRequest()
            )
        }
    }


}