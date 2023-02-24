package com.example.dipolia.presentation

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.dipolia.R
import com.example.dipolia.domain.DipolDomainEntity


@BindingAdapter("lableColor")
fun bindLableColor(view: View, colorList: List<Double>?) {
    Log.d("View", "colorlist $colorList $view ")
    val list = colorList ?: listOf(0.0, 0.0, 0.0)
     view.setBackgroundColor(Color.parseColor(colorToUI(list)))
}

private fun colorToUI(colorList: List<Double>): String {

    val string = String.format(
        "#" + "%02x%02x%02x",
        (colorList[0] * 255).toInt(),
        (colorList[1] * 255).toInt(),
        (colorList[2] * 255).toInt()
    )
    Log.d("bindLableColor", "colorToUI $string")
    return string
}

@BindingAdapter("selectedBackground")
fun setSelectedBackground(textView: TextView, isSelected: Boolean){
    if (isSelected) {
        textView.setBackgroundColor(ContextCompat.getColor(textView.context, R.color.colorAccent))
    } else {
        textView.setBackgroundColor(ContextCompat.getColor(textView.context, R.color.colorPrimaryDark))
    }
}

@BindingAdapter("selectedVisibility")
fun setSelectedVisibility(view: View, isSelected: Boolean){
    if (isSelected) {
        view.visibility = VISIBLE
    } else {
        view.visibility = INVISIBLE
    }
}

@BindingAdapter("selectedDipolControlLayoutVisibility")
fun setSelectedDipolLayoutVisibility(view: View, isConnected: Boolean){
    if (isConnected) {
        view.visibility = VISIBLE
    } else {
        view.visibility = INVISIBLE
    }
}

@BindingAdapter("selectedButtonRemoveVisibility")
fun setSelectedButtonRemoveVisibility(textView: TextView, list: List<DipolDomainEntity>?){
    if (list != null && list.isNotEmpty()) {
        textView.visibility = VISIBLE
    } else {
        textView.visibility = INVISIBLE
    }
}

@BindingAdapter("selectedDipolFrameLayoutVisibility")
fun setSelectedDipolAllLayoutVisibility(layout: FrameLayout, list: List<DipolDomainEntity>?){
    if (list != null && list.isNotEmpty()) {
        layout.visibility = VISIBLE
    } else {
        layout.visibility = INVISIBLE
    }
}


@BindingAdapter("workerButtonText")
fun setWorkerButtonText(textView: TextView, isRunning: Boolean){
    if (isRunning) {
        textView.text = textView.context.getString(R.string.background_work_stop)
    } else {
        textView.text = textView.context.getString(R.string.background_work_start)
    }
}
//@BindingAdapter("progressValue")
//fun bindProgressValue(seekBar: SeekBar, colorValue:Double?) {
//    Log.d("progressValue", "$colorValue")
////    colorValue?.let { seekBar.progress = colorValueToProgress(colorValue) }
//
//}

//private fun colorValueToProgress(colorValue: Double) :Int{
//    val progress = (colorValue*100).toInt()
//    return progress
//}