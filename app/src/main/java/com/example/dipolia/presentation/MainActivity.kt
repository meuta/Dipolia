package com.example.dipolia.presentation

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dipolia.databinding.ActivityLocalModeBinding
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.presentation.adaptes.DipolListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity() : AppCompatActivity() {

    private lateinit var localModeViewModel: LocalModeViewModel
    private val scope = CoroutineScope(Dispatchers.IO)

    private lateinit var binding: ActivityLocalModeBinding

    private lateinit var dipolListAdapter: DipolListAdapter

    private lateinit var seekBarList: List<SeekBar>
    private lateinit var seekBarFiveLightsList: List<SeekBar>
    private var selectedDipol: DipolDomainEntity? = null
    private var selectedLamp: LampDomainEntity? = null
    private var selectedConnectedLampType: LampType? = null
    private var selectedFiveLights: FiveLightsDomainEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        localModeViewModel = ViewModelProvider(this)[LocalModeViewModel::class.java]

        binding.viewModel = localModeViewModel
        binding.lifecycleOwner = this

//        localModeViewModel.allLampsList.observe(this) { list ->
//            Log.d("TEST_OF_SUBSCRIBE", "allLampsListSelectedItem: ${list.map { "${it.id}, ${it.selected}"}}")
//        }

//        localModeViewModel.dipolList.observe(this) {
////            Log.d("TEST_OF_SUBSCRIBE", "dipolList: $it")
//            dipolListAdapter.submitList(it)      // Created new thread
//        }

        localModeViewModel.myLamps.observe(this) {
            Log.d("TEST_OF_SUBSCRIBE", "myLamps: ${it.map { item -> "${item.id}, ${item.selected}, ${item.lastConnection}"} }")
        }

        localModeViewModel.myDipolsList.observe(this) {
            Log.d("TEST_OF_SUBSCRIBE", "dipolList: ${it.map { item -> "${item.id}, ${item.selected}, ${item.lastConnection}"} }")
            dipolListAdapter.submitList(it)      // Created new thread
        }

//        localModeViewModel.fiveLights.observe(this) {
////            Log.d("TEST_OF_SUBSCRIBE", "fiveLights: $it")
//            selectedFiveLights = it
//            setFiveLightsSeekbars(it)
//        }
//        localModeViewModel.myFiveLight.observe(this) {
//            Log.d("TEST_OF_SUBSCRIBE", "fiveLights: $it")
////            selectedFiveLights = it
////            setFiveLightsSeekbars(it)
//        }

//        localModeViewModel.selectedDipol.observe(this) {
//            selectedDipol = it
////            Log.d("TEST_OF_SUBSCRIBE", "selectedDipol: $it")
//            setSeekbarsForSelectedDipol(it)
//        }

//        localModeViewModel.selectedLamp.observe(this) {
//            selectedLamp = it
////            Log.d("TEST_OF_SUBSCRIBE", "selectedDipol: $it")
//        }

//        localModeViewModel.selectedConnectedLampType.observe(this) {
//            selectedConnectedLampType = it
//        }

//        localModeViewModel.isBackGroundWork.observe(this) {
////            Log.d("init", "fromMain: $it")
//        }




        binding.btnRefreshList.setOnClickListener {
            refreshConnectedList()
        }

        binding.btnUnselect.setOnClickListener {
            localModeViewModel.unselectLamp()
        }

        binding.btnBackgroundWork.setOnClickListener {
            localModeViewModel.workerStartStop()
//            Toast.makeText(this, "This button doesn't work now..", Toast.LENGTH_SHORT).show()
        }

        setupSeekbars()
        localModeViewModel.testSendLocalModeData()

    }


    private fun setupSeekbars() {
        val seekAdapter = object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                onUpdateSeekBar(seek)
                Log.d("seekAdapter", "onProgressChanged ${seek.id}")
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

            localSeekBarFiveLights1.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarFiveLights2.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarFiveLights3.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarFiveLights4.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarFiveLights5.setOnSeekBarChangeListener(seekAdapter)
            seekBarFiveLightsList = listOf(localSeekBarFiveLights1, localSeekBarFiveLights2, localSeekBarFiveLights3,localSeekBarFiveLights4, localSeekBarFiveLights5)

        }
//        Log.d("setupSeekbars", "seekBarList $seekBarList")
//        Log.d("setupSeekbars", "seekBarFiveLightsList $seekBarFiveLightsList")
    }


    private fun onUpdateSeekBar(seekBar: SeekBar) {

        val value: Int = seekBar.progress
        Log.d("onUpdateSeekBar", "value = $value")

        val valuePerCent = value / 100.0

        if (seekBar in seekBarList){
            val seekBarIndex = seekBarList.indexOf(seekBar)
            Log.d("onUpdateSeekBar", "seekBarIndex = $seekBarIndex")

            selectedDipol?.let {
                Log.d("onUpdateSeekBar", "selectedDipol = $it")

                localModeViewModel.changeLocalState("dipol", seekBarIndex, valuePerCent )
            }
        } else if (seekBar in seekBarFiveLightsList){
            val seekBarIndex = seekBarFiveLightsList.indexOf(seekBar)
            Log.d("onUpdateSeekBar", "seekBarFiveLightsIndex = $seekBarIndex")

            selectedFiveLights?.let {
                Log.d("onUpdateSeekBar", "selectedDipol = $it")

                localModeViewModel.changeLocalState("fiveLights", seekBarIndex, valuePerCent )
            }
        }
    }


    private fun setupRecyclerView() {

        dipolListAdapter = DipolListAdapter()
        binding.rvDipolItemList.adapter = dipolListAdapter
        setupClickListener()
    }

    private fun setupClickListener() {
        dipolListAdapter.onDipolItemClickListener = {
            localModeViewModel.selectLamp(it.id)
            Log.d("onDipolItemClickListener", "$it")
        }
    }

    private fun setSeekbarsForSelectedDipol(dipolDomainEntity: DipolDomainEntity?){
//        Log.d("onDipolItemClickListener", "Seekbars:$dipolDomainEntity")
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

    private fun setFiveLightsSeekbars(fiveLightsDomainEntity: FiveLightsDomainEntity?){
//        Log.d("onDipolItemClickListener", "setFiveLightsSeekbars")
        val fiveLights = fiveLightsDomainEntity ?: FiveLightsDomainEntity("","", listOf(0.0, 0.0, 0.0, 0.0, 0.0) )
        fiveLights.let {
            val progress1 = (it.c[0]*100).toInt()
            val progress2 = (it.c[1]*100).toInt()
            val progress3 = (it.c[2]*100).toInt()
            val progress4 = (it.c[3]*100).toInt()
            val progress5 = (it.c[4]*100).toInt()
            with(binding) {
                localSeekBarFiveLights1.progress = progress1
                localSeekBarFiveLights2.progress = progress2
                localSeekBarFiveLights3.progress = progress3
                localSeekBarFiveLights4.progress = progress4
                localSeekBarFiveLights5.progress = progress5
            }
        }

    }

    private fun refreshConnectedList() {
        localModeViewModel.refreshConnectedList()
    }

}