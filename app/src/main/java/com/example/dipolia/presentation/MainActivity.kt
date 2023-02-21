package com.example.dipolia.presentation

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dipolia.databinding.ActivityLocalModeBinding
import com.example.dipolia.databinding.ActivityMainBinding
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.presentation.adaptes.DipolListAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var localModeViewModel: LocalModeViewModel

//    private lateinit var binding: ActivityMainBinding
    private lateinit var binding: ActivityLocalModeBinding

    private lateinit var dipolListAdapter: DipolListAdapter

    private lateinit var seekBarList: List<SeekBar>
    private var selectedDipol: DipolDomainEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
        binding = ActivityLocalModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        localModeViewModel = ViewModelProvider(this)[LocalModeViewModel::class.java]

        binding.viewModel = localModeViewModel
        binding.lifecycleOwner = this

        localModeViewModel.selectedDipol.observe(this) {
            selectedDipol = it      // Created new thread
//            Log.d("TEST_OF_SUBSCRIBE", "selectedDipol: $it")
            setSeekbarsForSelectedDipol(it)
        }

        localModeViewModel.dipolList.observe(this) {
            dipolListAdapter.submitList(it)      // Created new thread
            Log.d("localModeViewModel.testSendLocalModeData(dipolID, string)", it.toString())
        }

//        binding.btnTest.setOnClickListener {
////            localModeViewModel.testSendLocalModeData(dipolID, string)
//            Toast.makeText(this, "TEST doesn't work now..", Toast.LENGTH_SHORT).show()
//        }

        binding.btnRefreshList.setOnClickListener {
            refreshConnectedList()
        }

        setupSeekbars()
//        refreshConnectedList()
        localModeViewModel.testSendLocalModeData()
//        refreshConnectedList()

    }


    private fun setupSeekbars() {
        val seekAdapter = object :
            SeekBar.OnSeekBarChangeListener {
            //            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                onUpdateSeekBar(seek)
//                val myId = seek.sourceLayoutResId
                Log.d("seekAdapter", "onProgressChanged ${seek.id}")
//                Log.d("seekAdapter","onProgressChanged ${myId}")
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
                onUpdateSeekBar(seek)
                Log.d("seekAdapter", "onStartTrackingTouch ${seek.id}.")
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
                onUpdateSeekBar(seek)
                Log.d("seekAdapter", "onStopTrackingTouch ${seek.id}")
            }
        }

        with(binding) {
            localSeekBar1.setOnSeekBarChangeListener(seekAdapter)
            localSeekBar2.setOnSeekBarChangeListener(seekAdapter)
            localSeekBar3.setOnSeekBarChangeListener(seekAdapter)
            localSeekBar4.setOnSeekBarChangeListener(seekAdapter)
            localSeekBar5.setOnSeekBarChangeListener(seekAdapter)
            localSeekBar6.setOnSeekBarChangeListener(seekAdapter)
            seekBarList = listOf(localSeekBar1, localSeekBar2, localSeekBar3,localSeekBar4, localSeekBar5, localSeekBar6)

        }
        Log.d("setupSeekbars", "seekBarList $seekBarList")
    }


    private fun onUpdateSeekBar(seekBar: SeekBar) {

        val value: Int = seekBar.progress
        Log.d("onUpdateSeekBar", "value = $value")

        val valuePerCent = value / 100.0

        val seekBarIndex = seekBarList.indexOf(seekBar)
        Log.d("onUpdateSeekBar", "seekBarIndex = $seekBarIndex")

//        val horn = when (seekBarIndex) {
//            in 0..2 -> Horn.FIRST
//            in 3..5 -> Horn.SECOND
//            else -> throw Exception("seekBarIndex is out of range")
//        }
//        val component  = when (seekBarIndex % 3) {
//            0 -> ColorComponent.RED
//            1 -> ColorComponent.GREEN
//            2 -> ColorComponent.BLUE
//            else -> throw Exception("seekBarIndex is out of range")
//        }
        selectedDipol?.let {
            Log.d("onUpdateSeekBar", "selectedDipol = $it")

            // Need to save to db firstly

            localModeViewModel.changeLocalState(seekBarIndex, valuePerCent )
        }

    }


    private fun setupRecyclerView() {

//        refreshConnectedList()

        dipolListAdapter = DipolListAdapter()
//        with(binding.rvDipolItemList) {
//            adapter = dipolListAdapter
//            recycledViewPool.setMaxRecycledViews(
//                DipolListAdapter.VIEW_TYPE_SELECTED,
//                DipolListAdapter.MAX_POOL_SIZE
//            )
//            recycledViewPool.setMaxRecycledViews(
//                DipolListAdapter.VIEW_TYPE_UNSELECTED,
//                DipolListAdapter.MAX_POOL_SIZE
//            )
//        }
        binding.rvDipolItemList.adapter = dipolListAdapter
        setupClickListener()
    }

    private fun setupClickListener() {
        dipolListAdapter.onDipolItemClickListener = {
            localModeViewModel.changeSelectedDipol(it.id)
            Log.d("onDipolItemClickListener", "$it")
//            setSeekbarsForSelectedDipol()
//            setSeekbarsForSelectedDipol(it)
        }
    }

    private fun setSeekbarsForSelectedDipol(dipolDomainEntity: DipolDomainEntity?){
//        val dipol = selectedDipol
        Log.d("onDipolItemClickListener", "Seekbars:$dipolDomainEntity")
        val dipol = dipolDomainEntity ?: DipolDomainEntity("","", listOf(0.0, 0.0, 0.0), listOf(0.0, 0.0, 0.0) )
        dipol.let {
            val progress1 = (it.c1[0]*100).toInt()
            val progress2 = (it.c1[1]*100).toInt()
            val progress3 = (it.c1[2]*100).toInt()
            val progress4 = (it.c2[0]*100).toInt()
            val progress5 = (it.c2[1]*100).toInt()
            val progress6 = (it.c2[2]*100).toInt()
            with(binding) {
                localSeekBar1.progress = progress1
                localSeekBar2.progress = progress2
                localSeekBar3.progress = progress3
                localSeekBar4.progress = progress4
                localSeekBar5.progress = progress5
                localSeekBar6.progress = progress6
            }
        }


    }

    private fun refreshConnectedList() {
        localModeViewModel.refreshConnectedList()
    }

}