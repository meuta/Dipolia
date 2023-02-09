package com.example.dipolia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dipolia.databinding.ActivityMainBinding
import com.example.dipolia.presentation.LocalModeViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var localModeViewModel: LocalModeViewModel

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        localModeViewModel = ViewModelProvider(this)[LocalModeViewModel::class.java]

        val dipolID = "b4e62d5316ce"
        val string = ""

        binding.btnTest.setOnClickListener {
            localModeViewModel.testSendLocalModeData(dipolID, string)
        }
    }
}