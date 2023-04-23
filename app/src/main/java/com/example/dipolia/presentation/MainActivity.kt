package com.example.dipolia.presentation

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dipolia.DipoliaApplication
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.databinding.ActivityLocalModeBinding
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.presentation.adaptes.DipolListAdapter
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var localModeViewModel: LocalModeViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (application as DipoliaApplication).component
    }

    private var mapper = DipoliaMapper()

    private lateinit var binding: ActivityLocalModeBinding

    private lateinit var dipolListAdapter: DipolListAdapter

    private lateinit var seekBarDipolList: List<SeekBar>
    private lateinit var seekBarFiveLightsList: List<SeekBar>
    private var selectedLamp: LampDomainEntity? = null
    private var currentLamps: List<LampDomainEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {

        component.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityLocalModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        localModeViewModel =
            ViewModelProvider(this, viewModelFactory)[LocalModeViewModel::class.java]

        binding.viewModel = localModeViewModel
        binding.lifecycleOwner = this



        localModeViewModel.myLampsLD.observe(this) {
            if (it.isNotEmpty()) {
                Log.d(
                    "TEST_OF_SUBSCRIBE",
                    "myLamps: ${it.map { item -> "${item.id}, ${item.selected}, ${item.lastConnection}" }}")
//                Log.d("TEST_OF_SUBSCRIBE", "myLamps: $it")
                currentLamps = it
            }
        }

        localModeViewModel.myDipolsListLD.observe(this) {
            Log.d(
                "TEST_OF_SUBSCRIBE",
                "dipolList: ${it.map { item -> "${item.id}, ${item.selected}, ${item.lastConnection}" }}")
//            Log.d("TEST_OF_SUBSCRIBE", "dipolList: $it")
            dipolListAdapter.submitList(it)      // Created new thread
        }

        localModeViewModel.myFiveLightListLD.observe(this) { list ->
            if (list.isNotEmpty()) {
                Log.d("TEST_OF_SUBSCRIBE", "fiveLights: ${list[0]}")
            }
        }

        localModeViewModel.selectedLampLD.observe(this) { lamp ->
            lamp?.let {
                Log.d("TEST_OF_SUBSCRIBE", "selectedLamp: ${lamp.id}, ${lamp.c} ")
                if (it.lampType == LampType.DIPOL) {
                    setDipolSeekbars(mapper.mapLampEntityToDipolEntity(it))
                } else if (it.lampType == LampType.FIVE_LIGHTS) {
                    setFiveLightsSeekbars(mapper.mapLampEntityToFiveLightsEntity(it))
                }
                selectedLamp = it
            }
        }

        localModeViewModel.isBackGroundWork.observe(this) {
            Log.d("TEST_OF_SUBSCRIBE", "isBackGroundWorker: $it")
        }


        with(binding) {
            btnRefreshList.setOnClickListener {
                refreshConnectedList()
            }

            btnUnselect.setOnClickListener {
                localModeViewModel.unselectLamp()
            }

            btnBackgroundWork.setOnClickListener {
                localModeViewModel.workerStartStop()
//            Toast.makeText(this, "This button doesn't work now..", Toast.LENGTH_SHORT).show()
            }

//            btnSaveLamp.setOnClickListener {
//                selectedLamp?.let {
//                    localModeViewModel.saveLamp(it)
//                    Toast.makeText(this@MainActivity, "@${it.lampType} colorSet have been saved", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            btnSaveLampList.setOnClickListener {
//                if (currentLamps.isNotEmpty()) {
//                    localModeViewModel.saveLampList(currentLamps)
//                    Toast.makeText(this@MainActivity, "Lamps have been saved", Toast.LENGTH_SHORT).show()
//                }
//            }
        }

        setupSeekbars()
//        localModeViewModel.testSendLocalModeData()
    }

    override fun onStop() {
        Log.d("onStop", "here")
        if (currentLamps.isNotEmpty()) {
            localModeViewModel.saveLampList(currentLamps)
            Toast.makeText(this@MainActivity, "Lamps have been saved", Toast.LENGTH_SHORT).show()
        }
        super.onStop()
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
            localSeekBarDipol1.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarDipol2.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarDipol3.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarDipol4.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarDipol5.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarDipol6.setOnSeekBarChangeListener(seekAdapter)
            seekBarDipolList = listOf(
                localSeekBarDipol1,
                localSeekBarDipol2,
                localSeekBarDipol3,
                localSeekBarDipol4,
                localSeekBarDipol5,
                localSeekBarDipol6
            )

            localSeekBarFiveLights1.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarFiveLights2.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarFiveLights3.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarFiveLights4.setOnSeekBarChangeListener(seekAdapter)
            localSeekBarFiveLights5.setOnSeekBarChangeListener(seekAdapter)
            seekBarFiveLightsList = listOf(
                localSeekBarFiveLights1,
                localSeekBarFiveLights2,
                localSeekBarFiveLights3,
                localSeekBarFiveLights4,
                localSeekBarFiveLights5
            )

        }
//        Log.d("setupSeekbars", "seekBarList $seekBarList")
//        Log.d("setupSeekbars", "seekBarFiveLightsList $seekBarFiveLightsList")
    }


    private fun onUpdateSeekBar(seekBar: SeekBar) {
        selectedLamp?.let {

            val value: Int = seekBar.progress
            Log.d("onUpdateSeekBar", "value = $value")

            val valuePerCent = value / 100.0

            var seekBarIndex = -1
            if (seekBar in seekBarDipolList) {
                seekBarIndex = seekBarDipolList.indexOf(seekBar)
                Log.d("onUpdateSeekBar", "seekBarIndex = $seekBarIndex")
            } else if (seekBar in seekBarFiveLightsList) {
                seekBarIndex = seekBarFiveLightsList.indexOf(seekBar)
                Log.d("onUpdateSeekBar", "seekBarFiveLightsIndex = $seekBarIndex")
            }
            Log.d(
                "onUpdateSeekBar",
                "selectedLamp = ${it.id}, lampType = ${it.lampType}, valuePerCent = $valuePerCent"
            )
            localModeViewModel.changeLocalState(it.id, seekBarIndex, valuePerCent)
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

    private fun setDipolSeekbars(dipolDomainEntity: DipolDomainEntity?) {
        Log.d("setDipolSeekbars", "Seekbars:$dipolDomainEntity")
        val dipol = dipolDomainEntity ?: DipolDomainEntity(
            "",
            "",
            listOf(0.0, 0.0, 0.0),
            listOf(0.0, 0.0, 0.0)
        )
        dipol.let {
            val progress1 = (it.c1[0] * 100).toInt()
            val progress2 = (it.c1[1] * 100).toInt()
            val progress3 = (it.c1[2] * 100).toInt()
            val progress4 = (it.c2[0] * 100).toInt()
            val progress5 = (it.c2[1] * 100).toInt()
            val progress6 = (it.c2[2] * 100).toInt()
            with(binding) {
                localSeekBarDipol1.progress = progress1
                localSeekBarDipol2.progress = progress2
                localSeekBarDipol3.progress = progress3
                localSeekBarDipol4.progress = progress4
                localSeekBarDipol5.progress = progress5
                localSeekBarDipol6.progress = progress6
            }
        }
    }

    private fun setFiveLightsSeekbars(fiveLightsDomainEntity: FiveLightsDomainEntity?) {
//        Log.d("onDipolItemClickListener", "setFiveLightsSeekbars")
        val fiveLights = fiveLightsDomainEntity ?: FiveLightsDomainEntity(
            "",
            "",
            listOf(0.0, 0.0, 0.0, 0.0, 0.0)
        )
        fiveLights.let {
            val progress1 = (it.c[0] * 100).toInt()
            val progress2 = (it.c[1] * 100).toInt()
            val progress3 = (it.c[2] * 100).toInt()
            val progress4 = (it.c[3] * 100).toInt()
            val progress5 = (it.c[4] * 100).toInt()
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
//        localModeViewModel.refreshConnectedList()
    }

}