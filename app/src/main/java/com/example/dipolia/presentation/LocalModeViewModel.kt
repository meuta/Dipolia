package com.example.dipolia.presentation

import androidx.lifecycle.ViewModel
import com.example.dipolia.data.DipoliaRepositoryImpl
import com.example.dipolia.domain.useCases.ReceiveLocalModeDataUseCase
import com.example.dipolia.domain.useCases.SendFollowMeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalModeViewModel: ViewModel() {

    private val repository = DipoliaRepositoryImpl
    private val receiveLocalModeDataUseCase = ReceiveLocalModeDataUseCase(repository)
    private val sendFollowMeUseCase = SendFollowMeUseCase(repository)

    private val scope = CoroutineScope(Dispatchers.IO)

    init {  //This code will be executes every time automatically with creating of this object
        scope.launch{
            sendFollowMeUseCase()
        }
        scope.launch{
            receiveLocalModeDataUseCase ()
        }
    }

}