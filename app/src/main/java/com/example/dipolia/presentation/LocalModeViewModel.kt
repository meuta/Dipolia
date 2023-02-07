package com.example.dipolia.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dipolia.data.DipoliaRepositoryImpl
import com.example.dipolia.domain.useCases.ReceiveLocalModeDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalModeViewModel: ViewModel() {

    private val repository = DipoliaRepositoryImpl
    private val receiveLocalModeDataUseCase = ReceiveLocalModeDataUseCase(repository)

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
//        loadData()     //This code will be executes every time automatically with creating of this object
        scope.launch{
            receiveLocalModeDataUseCase()
        }
    }

//    fun getUDPdData(itemId: Int) {
//        scope.launch {
//            val item = getShopItemByIdUseCase.getShopItemById(itemId)
//            _shopItem.value = item
//        }
//    }
}