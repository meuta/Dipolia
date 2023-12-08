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
                    setDipolSeekbarsProgress(mapper.mapLampEntityToDipolEntity(it))
                } else if (it.lampType == LampType.FIVE_LIGHTS) {
                    setFiveLightsSeekbarsProgress(mapper.mapLampEntityToFiveLightsEntity(it))
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
        val seekAdapter = object : OnSeekBarChangeListener {

            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                if (fromUser) { onUpdateSeekBar(seek) }
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
            seekBarDipolList = listOf(
                localSeekBarDipol1,
                localSeekBarDipol2,
                localSeekBarDipol3,
                localSeekBarDipol4,
                localSeekBarDipol5,
                localSeekBarDipol6
            ).map { it.apply { setOnSeekBarChangeListener(seekAdapter) } }

            seekBarFiveLightsList = listOf(
                localSeekBarFiveLights1,
                localSeekBarFiveLights2,
                localSeekBarFiveLights3,
                localSeekBarFiveLights4,
                localSeekBarFiveLights5
            ).map { it.apply { setOnSeekBarChangeListener(seekAdapter) } }
        }
    }

    private fun onUpdateSeekBar(seek: SeekBar) {
        var index: Int
        selectedLampId?.let {
            index = when (seek) {
                in seekBarDipolList -> seekBarDipolList.indexOf(seek)
                in seekBarFiveLightsList -> seekBarFiveLightsList.indexOf(seek)
                else -> -1
            }
            localModeViewModel.changeLocalState(it, index, seek.progress)
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


    private fun setDipolSeekbarsProgress(dipolDomainEntity: DipolDomainEntity?) {
        Log.d("setDipolSeekbars", "dipolDomainEntity = $dipolDomainEntity")
        val dipol = dipolDomainEntity ?: DipolDomainEntity(
            "",
            "",
            listOf(0.0, 0.0, 0.0),
            listOf(0.0, 0.0, 0.0)
        )
        dipol.let {lamp ->
            seekBarDipolList.take(3).mapIndexed { index, seek -> seek.progress =  (lamp.c1[index] * 100).toInt() }
            seekBarDipolList.takeLast(3).mapIndexed { index, seek -> seek.progress =  (lamp.c2[index] * 100).toInt() }
        }
    }

    private fun setFiveLightsSeekbarsProgress(fiveLightsDomainEntity: FiveLightsDomainEntity?) {
        Log.d("onDipolItemClickListener", "setFiveLightsSeekbars")
        val fiveLights = fiveLightsDomainEntity ?: FiveLightsDomainEntity(
            "",
            "",
            listOf(0.0, 0.0, 0.0, 0.0, 0.0)
        )
        fiveLights.let { lamp ->
            seekBarFiveLightsList.mapIndexed { index, seek -> seek.progress = (lamp.c[index] * 100).toInt() }
        }
    }


    override fun onStop() {
        Log.d("onStop", "here")
        localModeViewModel.saveLampList()
        Toast.makeText(this@MainActivity, "Lamps have been saved", Toast.LENGTH_SHORT).show()
        super.onStop()
    }

}