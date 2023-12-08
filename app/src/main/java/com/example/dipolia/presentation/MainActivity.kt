package com.example.dipolia.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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

    private var secondsChange: Double? = null
    private var secondsStay: Double? = null

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

        val doubleAdaptor = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val string = s.toString()
                val length = string.length
                val index = string.indexOf('.')
                if (index > -1 && index < length - 2) {
                    with(binding) {
                        if (etSecondsChange.text.hashCode() == s.hashCode()) {
                            dropLastSymbols(etSecondsChange, string)
                        } else if (etSecondsStay.text.hashCode() == s.hashCode()) {
                            dropLastSymbols(etSecondsStay, string)
                        }
                    }
                }
            }
        }

        with(binding) {
            etSecondsChange.addTextChangedListener(doubleAdaptor)
            etSecondsStay.addTextChangedListener(doubleAdaptor)
        }
    }

    private fun dropLastSymbols(et: EditText, string: String) {

        et.setText(string.dropLast(string.length - 2 - string.indexOf('.')))
        et.setSelection(et.text.length)
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
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                val isVisible = localModeViewModel.uiStateFlow.value.isLlLoopSettingsVisible
                if (isVisible == true) {
                    localModeViewModel.updateUiState(UiState(isLlLoopSettingsVisible = false))
                etSecondsChange.setText(secondsChange.toString())
                etSecondsStay.setText(secondsStay.toString())
                    inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
                    Log.d("btnLoopSettings", "$secondsChange")
                } else {
                    localModeViewModel.updateUiState(UiState(isLlLoopSettingsVisible = true))
                    etSecondsChange.requestFocus()
                    etSecondsChange.setSelection(etSecondsChange.text.length)
                    etSecondsStay.setSelection(etSecondsStay.text.length)
                    inputMethodManager.showSoftInput(etSecondsChange, 0)
                }
            }

            btnSaveLoopSettings.setOnClickListener {
                val secondsChange = etSecondsChange.text?.toString()?.toDoubleOrNull() ?: 0.0
                val secondsStay = etSecondsStay.text?.toString()?.toDoubleOrNull() ?: 0.0
                localModeViewModel.setLoopSeconds(
                    secondsChange.also { etSecondsChange.setText(it.toString()) },
                    secondsStay.also { etSecondsStay.setText(it.toString()) }
                )

                localModeViewModel.updateUiState(UiState(isLlLoopSettingsVisible = false))
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)

            }

            radioManual.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d("RADIO", "MANUAL is checked: $isChecked")
                localModeViewModel.setIsLooping(isLooping = !isChecked)
            }

            radioLoop.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d("RADIO", "LOOP is checked: $isChecked")
                localModeViewModel.setIsLooping(isLooping = isChecked)
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


        localModeViewModel.loopSecondsLD.observe(this) { pair ->
            Log.d("TEST_OF_SUBSCRIBE_LoopSettings", "Pair: $pair")
            Log.d("TEST_OF_SUBSCRIBE_LoopSettings", "change1 $secondsChange")
            Log.d("TEST_OF_SUBSCRIBE_LoopSettings", "stay1 $secondsStay")
//            secondsChange = pair.first ?: 0.0
            secondsChange = (pair.first ?: 0.0).also { binding.etSecondsChange.setText(it.toString()) }
            Log.d("TEST_OF_SUBSCRIBE_LoopSettings", "change2 $secondsChange")
//            secondsStay = pair.second ?: 0.0
            secondsStay = (pair.second ?: 0.0).also { binding.etSecondsStay.setText(it.toString()) }
            Log.d("TEST_OF_SUBSCRIBE_LoopSettings", "stay2 $secondsStay")
        }
    }

    private fun ActivityLocalModeBinding.setEditNameViews(oldLampName: String) {
        tvEditLampName.text = oldLampName
        etEditLampName.setText(oldLampName)
        etEditLampName.requestFocus()
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(etEditLampName, 0)

        if (llLoopSettings.visibility == View.VISIBLE) {
            etSecondsChange.setText(secondsChange.toString())
            etSecondsStay.setText(secondsStay.toString())
            localModeViewModel.updateUiState(UiState(isLlLoopSettingsVisible = false))
        }

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
        var seekBarIndex: Int
        val seekAdapter = object :
            OnSeekBarChangeListener {

            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                seekBarIndex = setSeekBarIndex(seek)
                if (fromUser) { onUpdateSeekBar(seek, seekBarIndex) }
                Log.d("seekAdapter", "onProgressChanged ${seek.id} fromUser = $fromUser")
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
                seekBarIndex = setSeekBarIndex(seek)
                onUpdateSeekBar(seek, seekBarIndex)
                Log.d("seekAdapter", "onStartTrackingTouch ${seek.id}.")
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
                seekBarIndex = setSeekBarIndex(seek)
                onUpdateSeekBar(seek, seekBarIndex)
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


    private fun setSeekBarIndex(seek: SeekBar): Int {
        return when (seek) {
            in seekBarDipolList -> { seekBarDipolList.indexOf(seek) }
            in seekBarFiveLightsList -> { seekBarFiveLightsList.indexOf(seek) }
            else -> { -1 }
        }
    }

    private fun onUpdateSeekBar(seekBar: SeekBar, seekBarIndex: Int) {
        selectedLampId?.let {
            localModeViewModel.changeLocalState(it, seekBarIndex, seekBar.progress)
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
        Log.d("onDipolItemClickListener", "setFiveLightsSeekbars")
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