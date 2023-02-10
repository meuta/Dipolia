package com.example.dipolia.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.dipolia.data.DipoliaRepositoryImpl
import com.example.dipolia.domain.useCases.GetDipolListUseCase
import com.example.dipolia.domain.useCases.ReceiveLocalModeDataUseCase
import com.example.dipolia.domain.useCases.SendFollowMeUseCase
import com.example.dipolia.domain.useCases.TestSendLocalModeDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalModeViewModel(application: Application): AndroidViewModel(application) {

    private val repository = DipoliaRepositoryImpl(application)
    private val receiveLocalModeDataUseCase = ReceiveLocalModeDataUseCase(repository)
    private val sendFollowMeUseCase = SendFollowMeUseCase(repository)
    private val testSendLocalModeDataUseCase = TestSendLocalModeDataUseCase(repository)
    private val getDipolListUseCase = GetDipolListUseCase(repository)

    private val scope = CoroutineScope(Dispatchers.IO)

    val dipolList = getDipolListUseCase()

    init {  //This code will be executes every time automatically with creating of this object
        scope.launch{
            sendFollowMeUseCase()
        }
        scope.launch{
            receiveLocalModeDataUseCase ()
            }
    }

    fun testSendLocalModeData(dipolID: String, string: String) {
        scope.launch {
            testSendLocalModeDataUseCase(dipolID, string)
        }
    }
}