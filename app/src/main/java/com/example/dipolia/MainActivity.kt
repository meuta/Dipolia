package com.example.dipolia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.dipolia.data.DipoliaRepositoryImpl
import com.example.dipolia.domain.useCases.ReceiveLocalModeDataUseCase
import com.example.dipolia.presentation.LocalModeViewModel
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var localModeViewModel: LocalModeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        runBlocking {
//            receiveLocalModeDataUseCase.invoke()
//        }
        localModeViewModel = ViewModelProvider(this)[LocalModeViewModel::class.java]


    }
}