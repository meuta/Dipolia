package com.example.dipolia.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.databinding.ActivityLocalModeBinding
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.domain.entities.LampType
import com.example.dipolia.presentation.adaptes.DipolListAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val localModeViewModel: LocalModeViewModel by viewModels()

    @Inject
    lateinit var mapper: DipoliaMapper

    private lateinit var binding: ActivityLocalModeBinding

    private lateinit var dipolListAdapter: DipolListAdapter

    private lateinit var seekBarDipolList: List<SeekBar>
    private lateinit var seekBarFiveLightsList: List<SeekBar>

    private var currentLamps: List<LampDomainEntity> = emptyList()

    private var selectedLampId: String? = null
    private var editableNameLampId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityLocalModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = localModeViewModel
        binding.lifecycleOwner = this

        setupRecyclerView()

        setupClickListener()
        setupLongClickListener()

        setupSeekbars()

        setupButtons()

        setupLoopSection()

        observeViewModel()

    }

    private fun setupLoopSection() {
        with(binding){

            etSecondsChange.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        val string = s.toString()
                        val length = string.length
                        val symbol = string.elementAtOrNull(length-3)
                        if (symbol == '.'){
                            etSecondsChange.setText(s?.dropLast(1))
                            etSecondsChange.setSelection(etSecondsChange.text.length)
                        }
                    }
                })

            etSecondsStay.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        val string = s.toString()
                        val length = string.length
                        val symbol = string.elementAtOrNull(length-3)
                        if (symbol == '.'){
                            etSecondsStay.setText(s?.dropLast(1))
                            etSecondsStay.setSelection(etSecondsStay.text.length)
                        }
                    }
                })
        }
    }


    private fun setupButtons() {
        with(binding) {

            btnUnselect.setOnClickListener {
                localModeViewModel.unselectLamp()
            }

            btnBackgroundWork.setOnClickListener {
                localModeViewModel.workerStartStop()
            }

            btnLoopSettings.setOnClickListener {
                if (llLoopSettings.visibility == View.VISIBLE) {
                    llLoopSettings.visibility = View.INVISIBLE
                } else if (llLoopSettings.visibility == View.INVISIBLE) {
                    llLoopSettings.visibility = View.VISIBLE
                }
                etSecondsChange.requestFocus()
                etSecondsChange.setSelection(etSecondsChange.text.length)

                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(etSecondsChange, 0)
            }

            btnSaveLoopSettings.setOnClickListener {
                val newSecondsChange = etSecondsChange.text?.toString()?.toDoubleOrNull()
                if (newSecondsChange == null) {
                    etSecondsChange.setText("0.0")
                } else {
                    etSecondsChange.setText(newSecondsChange.toDouble().toString())
                }

                val newSecondsStay: Double? = etSecondsStay.text?.toString()?.toDoubleOrNull()
                if (newSecondsStay == null) {
                    etSecondsStay.setText("0.0")
                } else {
                    etSecondsStay.setText(newSecondsStay.toDouble().toString())
                }

                Log.d("btnSaveLoopSettings", "newSecondsChange = $newSecondsChange")
                Log.d("btnSaveLoopSettings", "newSecondsStay = $newSecondsStay")
                localModeViewModel.updateStreamingState(
                    StreamingState(
                        secondsChange = newSecondsChange,
                        secondsStay = newSecondsStay
                    )
                )
                llLoopSettings.visibility = View.INVISIBLE
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            }

            radioManual.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d("RADIO", "MANUAL is checked: $isChecked")
                localModeViewModel.updateStreamingState(StreamingState(isLooping = !isChecked))

            }
            radioLoop.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d("RADIO", "LOOP is checked: $isChecked")
                localModeViewModel.updateStreamingState(StreamingState(isLooping = isChecked))
            }

            btnSaveLampName.setOnClickListener {
                val newName = etEditLampName.text.toString()
                editableNameLampId?.let { id ->
                    localModeViewModel.editLampName(id, newName)
                }
                exitEditNameViews(it)
            }

            btnCancelSaveLampName.setOnClickListener {
                exitEditNameViews(it)
            }
        }
    }

    private fun observeViewModel() {
        localModeViewModel.myLampsLD.observe(this) {
            if (it.isNotEmpty()) {
                Log.d(
                    "TEST_OF_SUBSCRIBE",
                    "myLamps: ${it.map { item -> "${item.id}, ${item.selected}, ${item.c}" }}"
                )
                currentLamps = it
            }
        }

        localModeViewModel.myDipolsListLD.observe(this) {
            Log.d(
                "TEST_OF_SUBSCRIBE",
                "dipolList: ${it.map { item -> "${item.id}, ${item.selected}, ${item.c1}, ${item.c2}" }}"
            )
            dipolListAdapter.submitList(it)
        }

        localModeViewModel.myFiveLightListLD.observe(this) { list ->
            if (list.isNotEmpty()) {
                Log.d("TEST_OF_SUBSCRIBE", "fiveLights: ${list[0].id} ${list[0].lampName}")
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
                selectedLampId = it.id
            }
        }

        localModeViewModel.isBackGroundWork.observe(this) {
            Log.d("TEST_OF_SUBSCRIBE", "isBackGroundWorker: $it")
        }
    }

    private fun ActivityLocalModeBinding.setEditNameViews(oldLampName: String) {
        tvEditLampName.text = oldLampName
        etEditLampName.setText(oldLampName)
        etEditLampName.requestFocus()
        val inputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(etEditLampName, 0)
        llEditLampName.visibility = View.VISIBLE
        currentLampLayout.visibility = View.INVISIBLE
        llButtons.visibility = View.INVISIBLE
        llEditLampNameButtons.visibility = View.VISIBLE
    }

    private fun ActivityLocalModeBinding.exitEditNameViews(it: View) {
        llEditLampName.visibility = View.INVISIBLE
        currentLampLayout.visibility = View.VISIBLE
        etEditLampName.setText("")
        etEditLampName.clearFocus()
        llButtons.visibility = View.VISIBLE
        llEditLampNameButtons.visibility = View.INVISIBLE
        val inputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        editableNameLampId = null
    }


    private fun setupSeekbars() {
        val seekAdapter = object :
            OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                if (fromUser) {
                    onUpdateSeekBar(seek)
                }
                Log.d("seekAdapter", "onProgressChanged ${seek.id} fromUser = $fromUser")
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
    }

    private fun onUpdateSeekBar(seekBar: SeekBar) {
        val progress = seekBar.progress
        Log.d("onUpdateSeekBar", "progress = $progress")

        var seekBarIndex = -1
        if (seekBar in seekBarDipolList) {
            seekBarIndex = seekBarDipolList.indexOf(seekBar)
            Log.d("onUpdateSeekBar", "seekBarIndex = $seekBarIndex")
        } else if (seekBar in seekBarFiveLightsList) {
            seekBarIndex = seekBarFiveLightsList.indexOf(seekBar)
            Log.d("onUpdateSeekBar", "seekBarFiveLightsIndex = $seekBarIndex")
        }
        val id = currentLamps.find { lamp -> lamp.selected }?.id

        id?.let {
            localModeViewModel.changeLocalState(it, seekBarIndex, progress)
        }
    }


    private fun setupRecyclerView() {

        dipolListAdapter = DipolListAdapter()
        binding.rvDipolItemList.adapter = dipolListAdapter
    }

    private fun setupClickListener() {
        dipolListAdapter.onDipolItemClickListener = {
            selectLamp(it.id)
            Log.d("onDipolItemClickListener", "$it")
        }
        with(binding) {
            cardViewFiveLights.setOnClickListener {
                selectLamp(currentLamps.find { lamp -> lamp.lampType == LampType.FIVE_LIGHTS }?.id)
                Log.d("cardViewFiveLights", ".setOnClickListener")
            }
            tvFiveLightsItem.setOnClickListener {
                selectLamp(currentLamps.find { lamp -> lamp.lampType == LampType.FIVE_LIGHTS }?.id)
                Log.d("cardViewFiveLights", ".setOnClickListener")
            }
        }
    }


    private fun selectLamp(lampId: String?) {
        lampId?.let { localModeViewModel.selectLamp(lampId) }
    }

    private fun setupLongClickListener() {
        dipolListAdapter.onDipolItemLongClickListener = {
            Log.d("setupLongClickListener", "dipolListAdapter.setOnLongClickListener")
            editableNameLampId = it.id
            val oldLampName = it.currentLampName

            binding.setEditNameViews(oldLampName)
        }

        binding.tvFiveLightsItem.setOnLongClickListener {
            Log.d("setupLongClickListener", "flFiveLightsItem.setOnLongClickListener")
            val id = currentLamps.find { lamp -> lamp.lampType == LampType.FIVE_LIGHTS }?.id
            editableNameLampId = id
            val textView = it as TextView
            val oldLampName = textView.text.toString()

            binding.setEditNameViews(oldLampName)

            true
        }
    }


    private fun setDipolSeekbars(dipolDomainEntity: DipolDomainEntity?) {
        Log.d("setDipolSeekbars", "dipolDomainEntity = $dipolDomainEntity")
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


    override fun onStop() {
        Log.d("onStop", "here")
        localModeViewModel.saveLampList()
        Toast.makeText(this@MainActivity, "Lamps have been saved", Toast.LENGTH_SHORT).show()
        super.onStop()
    }
}