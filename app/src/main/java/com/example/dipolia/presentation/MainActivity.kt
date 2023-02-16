package com.example.dipolia.presentation

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dipolia.databinding.ActivityMainBinding
import com.example.dipolia.presentation.adaptes.DipolListAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var localModeViewModel: LocalModeViewModel

    private lateinit var binding: ActivityMainBinding

    private lateinit var dipolListAdapter: DipolListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        localModeViewModel = ViewModelProvider(this)[LocalModeViewModel::class.java]

        setupRecyclerView()

        localModeViewModel.dipolList.observe(this) {
            dipolListAdapter.submitList(it)      // Created new thread
            Log.d("TEST_OF_SUBSCRIBE", it.toString())
        }

        val dipolID = "b4e62d5316ce"
        val string = ""

        binding.btnTest.setOnClickListener {
            localModeViewModel.testSendLocalModeData(dipolID, string)
        }

        binding.btnRefreshList.setOnClickListener {
            refreshConnectedList()
        }
    }

    private fun setupRecyclerView() {

        refreshConnectedList()

        dipolListAdapter = DipolListAdapter()
        with(binding.rvDipolItemList) {
            adapter = dipolListAdapter

            recycledViewPool.setMaxRecycledViews(
                DipolListAdapter.VIEW_TYPE_SELECTED,
                DipolListAdapter.MAX_POOL_SIZE
            )
            recycledViewPool.setMaxRecycledViews(
                DipolListAdapter.VIEW_TYPE_UNSELECTED,
                DipolListAdapter.MAX_POOL_SIZE
            )
        }

        setupClickListener()
    }

    private fun setupClickListener() {
        dipolListAdapter.onDipolItemClickListener = {
            localModeViewModel.changeSelectedDipol(it.id)
        }
    }

    private fun refreshConnectedList(){
        localModeViewModel.refreshConnectedList()
    }

}