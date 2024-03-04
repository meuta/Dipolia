package com.example.dipolia.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.databinding.ActivityLocalModeBinding
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity
import com.example.dipolia.presentation.adaptes.DipolListAdapter
import com.example.dipolia.presentation.adaptes.FiveLightsListAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val localModeViewModel: LocalModeViewModel by viewModels()

    @Inject
    lateinit var mapper: DipoliaMapper

    private lateinit var binding: ActivityLocalModeBinding

    private lateinit var dipolListAdapter: DipolListAdapter
    private lateinit var fiveLightsListAdapter: FiveLightsListAdapter

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
            with(binding) {
                if (localModeViewModel.uiStateFlow.value.isLlLoopSettingsVisible == true) {

                    localModeViewModel.updateUiState(UiState(isLlLoopSettingsVisible = false))
                    etSecondsChange.setText(localModeViewModel.loopSecondsFlow.value.first.toString())
                    etSecondsStay.setText(localModeViewModel.loopSecondsFlow.value.second.toString())
//                    Log.d("btnLoopSettings", "$secondsChange")
                    binding.enableRecyclerView()                }
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
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                val isVisible = localModeViewModel.uiStateFlow.value.isLlLoopSettingsVisible
                if (isVisible == true) {
                    localModeViewModel.updateUiState(UiState(isLlLoopSettingsVisible = false))
                    etSecondsChange.setText(localModeViewModel.loopSecondsFlow.value.first.toString())
                    etSecondsStay.setText(localModeViewModel.loopSecondsFlow.value.second.toString())
                    inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
//                    Log.d("btnLoopSettings", "$secondsChange")
                    binding.enableRecyclerView()
                } else {
                    localModeViewModel.updateUiState(UiState(isLlLoopSettingsVisible = true))
                    etSecondsChange.requestFocus()
                    etSecondsChange.setSelection(etSecondsChange.text.length)
                    etSecondsStay.setSelection(etSecondsStay.text.length)
                    inputMethodManager.showSoftInput(etSecondsChange, 0)

                    binding.disableRecyclerView()
                }
            }

            btnSaveLoopSettings.setOnClickListener {
                val secondsChange = etSecondsChange.text?.toString()?.toDoubleOrNull() ?: 0.0
                val secondsStay = etSecondsStay.text?.toString()?.toDoubleOrNull() ?: 0.0
                localModeViewModel.setLoopSeconds(secondsChange, secondsStay)

                localModeViewModel.updateUiState(UiState(isLlLoopSettingsVisible = false))
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)

                binding.enableRecyclerView()
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
                binding.exitEditNameViews(it)
            }

            btnCancelSaveLampName.setOnClickListener {
                binding.exitEditNameViews(it)
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

        localModeViewModel.myDipolsListLD.observe(this) {list ->
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

    }

    private fun ActivityLocalModeBinding.setEditNameViews(oldLampName: String) {
        tvEditLampName.text = oldLampName
        etEditLampName.setText(oldLampName)
        etEditLampName.requestFocus()
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
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
        val seekAdapter = object : OnSeekBarChangeListener {

            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                if (fromUser) { onUpdateSeekBar(seek) }
//                Log.d("seekAdapter", "onProgressChanged ${seek.id} fromUser = $fromUser")
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
                onUpdateSeekBar(seek)
//                Log.d("seekAdapter", "onStartTrackingTouch ${seek.id}.")
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
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
            if (localModeViewModel.uiStateFlow.value.isLlLoopSettingsVisible == true) {
                child?.isEnabled = false
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
        dipol.let {lamp ->
            seekBarDipolList.take(3).mapIndexed { index, seek -> seek.progress =  (lamp.c1[index] * 100).toInt() }
            seekBarDipolList.takeLast(3).mapIndexed { index, seek -> seek.progress =  (lamp.c2[index] * 100).toInt() }
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
            seekBarFiveLightsList.mapIndexed { index, seek -> seek.progress = (lamp.c[index] * 100).toInt() }
        }
    }


    override fun onStop() {
//        Log.d("onStop", "here")
        localModeViewModel.saveLampList()
        Toast.makeText(this@MainActivity, "Lamps have been saved", Toast.LENGTH_SHORT).show()
        super.onStop()
    }



    companion object{

        private const val TAG = "MainActivity"
    }
}