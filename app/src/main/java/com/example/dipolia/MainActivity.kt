package com.example.dipolia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dipolia.presentation.LocalModeViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var localModeViewModel: LocalModeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        localModeViewModel = ViewModelProvider(this)[LocalModeViewModel::class.java]

    }
}