package com.example.dipolia.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.databinding.ActivityLocalModeBinding
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.presentation.adaptes.DipolListAdapter
import com.example.dipolia.presentation.adaptes.FiveLightsListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private val localModeViewModel: LocalModeViewModel by viewModels()

    @Inject
    lateinit var mapper: DipoliaMapper

    private lateinit var binding: ActivityLocalModeBinding

    private lateinit var inputMethodManager : InputMethodManager

    private lateinit var dipolListAdapter: DipolListAdapter
    private lateinit var fiveLightsListAdapter: FiveLightsListAdapter

    private lateinit var seekBarDipolList: List<SeekBar>
    private lateinit var seekBarFiveLightsList: List<SeekBar>

    private var currentLamps: List<LampDomainEntity> = emptyList()

    private var selectedLampId: String? = null
    private var editableNameLampId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

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

        setOnBackPressedCallback()
    }

    private fun setOnBackPressedCallback() {
        onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback
        )
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(
        true
    ) {
        override fun handleOnBackPressed() {
            lifecycleScope.launch {
                localModeViewModel.uiStateFlow.first {
                    Log.d(TAG, "handleOnBackPressed: uiStateFlow.collect = $it ")
                    if (it.isLlLoopSettingsVisible) {
                        localModeViewModel.updateUiState(UiState(isLlLoopSettingsVisible = false))
                    }
                    true
                }
            }
        }
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
                lifecycleScope.launch {
                    localModeViewModel.uiStateFlow.first {
                        Log.d(TAG, "btnLoopSettings: uiStateFlow.collect = $it ")
                        if (it.isLlLoopSettingsVisible) {
                            localModeViewModel.updateUiState(UiState(isLlLoopSettingsVisible = false))
                        } else {
                            localModeViewModel.updateUiState(UiState(isLlLoopSettingsVisible = true))
                        }
                        true
                    }
                }
            }

            btnSaveLoopSettings.setOnClickListener {
                val secondsChange = etSecondsChange.text?.toString()?.toDoubleOrNull() ?: 0.0
                val secondsStay = etSecondsStay.text?.toString()?.toDoubleOrNull() ?: 0.0

                lifecycleScope.launch {
                    localModeViewModel.loopSecondsFlow.first {
                        Log.d(TAG, "btnSaveLoopSettings: loopSecondsFlow.first = $it ")

                        localModeViewModel.setLoopSeconds(secondsChange, secondsStay)

                        var doNotRefreshETSecondsChange = false
                        var doNotRefreshETSecondsStay = false
                        if (it.first != secondsChange || etSecondsChange.text?.toString() == secondsChange.toString()) {
                            Log.d(TAG, "btnSaveLoopSettings: first if ")
                            doNotRefreshETSecondsChange = true
                        }
                        if (it.second != secondsStay || etSecondsStay.text?.toString() == secondsStay.toString()) {
                            Log.d(TAG, "btnSaveLoopSettings: second if ")
                            doNotRefreshETSecondsStay = true
                        }

                        localModeViewModel.updateUiState(
                            UiState(
                                isLlLoopSettingsVisible = false,
                                doNotUpdateETSecondsChange = doNotRefreshETSecondsChange,
                                doNotUpdateETSecondsStay = doNotRefreshETSecondsStay
                            )
                        )
                        true
                    }
                }
            }

            radioManual.setOnCheckedChangeListener { buttonView, isChecked ->
//                Log.d("RADIO", "MANUAL is checked: $isChecked")
                localModeViewModel.setIsLooping(isLooping = !isChecked)
            }

            radioLoop.setOnCheckedChangeListener { buttonView, isChecked ->
//                Log.d("RADIO", "LOOP is checked: $isChecked")
                localModeViewModel.setIsLooping(isLooping = isChecked)
            }

            btnSaveLampName.setOnClickListener {
                val newName = etEditLampName.text.toString()
                editableNameLampId?.let { id ->
                    localModeViewModel.editLampName(id, newName)
                }
                binding.exitEditNameViews()
            }

            btnCancelSaveLampName.setOnClickListener {
                binding.exitEditNameViews()
            }
        }
    }

    private fun ActivityLocalModeBinding.disableRecyclerView() {
        rvDipolItemList.forEach { it.isEnabled = false }
        rvFiveLightsItemList.forEach { it.isEnabled = false }
        btnUnselect.isEnabled = false
    }


    private fun ActivityLocalModeBinding.enableRecyclerView() {
        rvDipolItemList.forEach { it.isEnabled = true }
        rvFiveLightsItemList.forEach { it.isEnabled = true }
        btnUnselect.isEnabled = true
    }


    private fun observeViewModel() {
        localModeViewModel.myLampsLD.observe(this) {
            if (it.isNotEmpty()) {
//                Log.d(
//                    "TEST_OF_SUBSCRIBE",
//                    "myLamps: ${it.map { item -> "${item.id}, ${item.selected}, ${item.c}" }}"
//                )
                currentLamps = it
            }
        }

        localModeViewModel.myDipolsListLD.observe(this) { list ->
//            Log.d(
//                "TEST_OF_SUBSCRIBE",
//                "dipolList: ${list.map { item -> "${item.id}, ${item.selected}, ${item.c1}, ${item.c2}" }}"
//            )
            dipolListAdapter.submitList(list)
        }

        localModeViewModel.myFiveLightListLD.observe(this) { list ->
//                Log.d(
//                    "TEST_OF_SUBSCRIBE",
//                    "fiveLightsList: ${list.map { item -> "${item.id}, ${item.selected}, ${item.c}" }}"
//                )
            fiveLightsListAdapter.submitList(list)
        }


        localModeViewModel.selectedDipolLD.observe(this) { dipol ->
//            Log.d("TEST_OF_SUBSCRIBE", "selectedLamp: dipol ${dipol?.lampName}")
            dipol?.let {
                setDipolSeekbarsProgress(dipol)
                selectedLampId = dipol.id
            }
        }

        localModeViewModel.selectedFiveLightsLD.observe(this) { fiveLights ->
//            Log.d("TEST_OF_SUBSCRIBE", "selectedLamp: fiveLights ${fiveLights?.lampName}")
            fiveLights?.let {
                setFiveLightsSeekbarsProgress(fiveLights)
                selectedLampId = fiveLights.id
            }
        }


        localModeViewModel.isBackGroundWork.observe(this) {
//            Log.d("TEST_OF_SUBSCRIBE", "isBackGroundWorker: $it")
        }

        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
                localModeViewModel.uiStateFlow.collect{ uiState ->
//            Log.d("TEST_OF_SUBSCRIBE", "isVisible: ${uiState.isLlLoopSettingsVisible}")
                    if (!uiState.isLlLoopSettingsVisible) {

                        with(binding) {
                            inputMethodManager.hideSoftInputFromWindow(llLoopSettings.windowToken, 0)
                            if (!uiState.doNotUpdateETSecondsChange) {
                                lifecycleScope.launch {
                                    localModeViewModel.loopSecondsFlow.first {
                                        Log.d(TAG, "UpdateETSecondsChange: loopSecondsFlow.first = $it")
                                        etSecondsChange.setText(it.first.toString())
                                        true
                                    }
                                }
                            }
                            if (!uiState.doNotUpdateETSecondsStay) {
                                lifecycleScope.launch {
                                    localModeViewModel.loopSecondsFlow.first {
                                        Log.d(TAG, "UpdateETSecondsStay: loopSecondsFlow.first = $it")
                                        etSecondsStay.setText(it.second.toString())
                                        true
                                    }
                                }
                            }

                            enableRecyclerView()
                        }
                    } else {
                        with(binding) {
                            etSecondsChange.requestFocus()
                            etSecondsChange.setSelection(etSecondsChange.text.length)
                            etSecondsStay.setSelection(etSecondsStay.text.length)
                            inputMethodManager.showSoftInput(etSecondsChange, 0)
                            disableRecyclerView()
                        }
                    }
                }
            }
        }
    }

    private fun ActivityLocalModeBinding.setEditNameViews(oldLampName: String) {
        tvEditLampName.text = oldLampName
        etEditLampName.setText(oldLampName)
        etEditLampName.requestFocus()
        inputMethodManager.showSoftInput(etEditLampName, 0)

        llEditLampName.visibility = View.VISIBLE
        llButtons.visibility = View.INVISIBLE
        llEditLampNameButtons.visibility = View.VISIBLE
    }

    private fun ActivityLocalModeBinding.exitEditNameViews() {
        llEditLampName.visibility = View.INVISIBLE
        etEditLampName.setText("")
        etEditLampName.clearFocus()
        llButtons.visibility = View.VISIBLE
        llEditLampNameButtons.visibility = View.INVISIBLE
        inputMethodManager.hideSoftInputFromWindow(llEditLampName.windowToken, 0)
        editableNameLampId = null
    }


    private fun setupSeekbars() {
        val seekAdapter = object : OnSeekBarChangeListener {

            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    onUpdateSeekBar(seek)
                }
//                Log.d("seekAdapter", "onProgressChanged ${seek.id} fromUser = $fromUser")
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                onUpdateSeekBar(seek)
//                Log.d("seekAdapter", "onStartTrackingTouch ${seek.id}.")
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                onUpdateSeekBar(seek)
//                Log.d("seekAdapter", "onStopTrackingTouch ${seek.id}")
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
        fiveLightsListAdapter = FiveLightsListAdapter()
        binding.rvDipolItemList.adapter = dipolListAdapter
        binding.rvFiveLightsItemList.adapter = fiveLightsListAdapter

        binding.rvDipolItemList.setOnHierarchyChangeListener(addNewItemListener)
        binding.rvFiveLightsItemList.setOnHierarchyChangeListener(addNewItemListener)
    }

    private val addNewItemListener = object : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewAdded(parent: View?, child: View?) {
            lifecycleScope.launch {
                localModeViewModel.uiStateFlow.first {
                    Log.d(TAG, "addNewItemListener: uiStateFlow.collect = $it ")
                    if (it.isLlLoopSettingsVisible) {
                        child?.isEnabled = false
                    }
                    true
                }
            }
        }

        override fun onChildViewRemoved(parent: View?, child: View?) {
        }
    }

    private fun setupClickListener() {
        dipolListAdapter.onDipolItemClickListener = {
//            Log.d("onDipolItemClickListener", "select: ${it.id}, ${it.lampName}")
            selectLamp(it.id)
        }
        fiveLightsListAdapter.onFiveLightsItemClickListener = {
//            Log.d("onFiveLightsItemClickListener", "select: ${it.id}, ${it.lampName}")
            selectLamp(it.id)
        }
    }


    private fun selectLamp(lampId: String?) {
        lampId?.let { localModeViewModel.selectLamp(lampId) }
    }

    private fun setupLongClickListener() {
        dipolListAdapter.onDipolItemLongClickListener = {
//            Log.d("setupLongClickListener", "dipolListAdapter.setOnLongClickListener")
            editableNameLampId = it.id
            val oldLampName = it.currentLampName

            binding.setEditNameViews(oldLampName)
        }

        fiveLightsListAdapter.onFiveLightsItemLongClickListener = {
//            Log.d("setupLongClickListener", "fiveLightsListAdapter.setOnLongClickListener")
            editableNameLampId = it.id
            val oldLampName = it.currentLampName

            binding.setEditNameViews(oldLampName)
        }
    }


    private fun setDipolSeekbarsProgress(dipolDomainEntity: DipolDomainEntity?) {
//        Log.d("setDipolSeekbars", "dipolDomainEntity = $dipolDomainEntity")
        val dipol = dipolDomainEntity ?: DipolDomainEntity(
            "",
            "",
            listOf(0.0, 0.0, 0.0),
            listOf(0.0, 0.0, 0.0)
        )
        dipol.let { lamp ->
            seekBarDipolList.take(3)
                .mapIndexed { index, seek -> seek.progress = (lamp.c1[index] * 100).toInt() }
            seekBarDipolList.takeLast(3)
                .mapIndexed { index, seek -> seek.progress = (lamp.c2[index] * 100).toInt() }
        }
    }

    private fun setFiveLightsSeekbarsProgress(fiveLightsDomainEntity: FiveLightsDomainEntity?) {
//        Log.d("setFiveLightsSeekbars", "fiveLightsDomainEntity = $fiveLightsDomainEntity")
        val fiveLights = fiveLightsDomainEntity ?: FiveLightsDomainEntity(
            "",
            "",
            listOf(0.0, 0.0, 0.0, 0.0, 0.0)
        )
        fiveLights.let { lamp ->
            seekBarFiveLightsList.mapIndexed { index, seek ->
                seek.progress = (lamp.c[index] * 100).toInt()
            }
        }
    }


    override fun onStop() {
//        Log.d("onStop", "here")
        localModeViewModel.saveLampList()
        Toast.makeText(this@MainActivity, "Lamps have been saved", Toast.LENGTH_SHORT).show()
        super.onStop()
    }


    companion object {

        private const val TAG = "MainActivity"
    }
}