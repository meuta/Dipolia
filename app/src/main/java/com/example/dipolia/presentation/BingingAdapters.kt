package com.example.dipolia.presentation

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.dipolia.R
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.entities.LampType


interface OnFiveLightsClickListener{
    fun onClick(id: String)
}

@BindingAdapter("dipolLableColor")
fun bindDipolLableColor(view: View, colorList: List<Double>?) {
//    Log.d("View", "colorlist $colorList $view ")
    val list = colorList ?: listOf(0.0, 0.0, 0.0)
     view.setBackgroundColor(Color.parseColor(colorToUI(list)))
}

@BindingAdapter("fiveLightsLableColor")
fun bindFiveLightsLableColor(view: View, colorList: List<Double>?) {
//    Log.d("View", "colorlist $colorList $view ")
    val list = colorList ?: listOf(0.0, 0.0, 0.0, 0.0, 0.0)
     view.setBackgroundColor(Color.parseColor(colorFiveLightsToUI(list)))
}

private fun colorFiveLightsToUI(colorList: List<Double>): String {

    val string = String.format(
        "#" + "%02x%02x%02x",
        (colorList[0] * 255 * 2 / 3 + colorList[3] * 255 / 3).toInt(),
        (colorList[1] * 255 * 2 / 3 + colorList[3] * 255 / 3).toInt(),
        (colorList[2] * 255 * 2 / 3 + colorList[3] * 255 / 3).toInt()
//        (colorList[3] * 255).toInt(),
//        (colorList[4] * 255).toInt()
    )
//    Log.d("bindFiveLightsLableColor", "colorToUI $string")
    return string
}

private fun colorToUI(colorList: List<Double>): String {

    val string = String.format(
        "#" + "%02x%02x%02x",
        (colorList[0] * 255).toInt(),
        (colorList[1] * 255).toInt(),
        (colorList[2] * 255).toInt()
    )
//    Log.d("bindLableColor", "colorToUI $string")
    return string
}

@BindingAdapter("fiveLightsItemVisibility")
fun setFiveLightsItemVisibility(view: View, isConnected: Boolean){
    if (isConnected) {
        view.visibility = VISIBLE
    } else {
        view.visibility = INVISIBLE
    }
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

@BindingAdapter("lampControlLayoutVisibility")
fun setLampControlLayoutVisibility(view: View, isConnected: Boolean){
//    Log.d("setSelectedDipolLayoutVisibility", "$isConnected")
    if (isConnected) {
        view.visibility = VISIBLE
    } else {
        view.visibility = INVISIBLE
    }
}


@BindingAdapter("pleaseSelectTextViewVisibility")
fun setPleaseSelectTextViewVisibility(view: View, lampType: LampType?){
//    Log.d("setSelectedPleaseSelectTextViewVisibility", "$lampType")
    if (lampType == LampType.DIPOl || lampType == LampType.FIVE_LIGHTS) {
        view.visibility = VISIBLE
    } else {
        view.visibility = INVISIBLE
    }
}

@BindingAdapter("dipolControlLayoutVisibility")
fun setDipolControlLayoutVisibility(view: View, lampType: LampType?){
//    Log.d("setDipolControlLayoutVisibility", "$lampType")
    if (lampType == LampType.DIPOl) {
        view.visibility = VISIBLE
    } else {
        view.visibility = INVISIBLE
    }
}

@BindingAdapter("fiveLightsControlLayoutVisibility")
fun setFiveLightsControlLayoutVisibility(view: View, lampType: LampType?){
//    Log.d("setFiveLightsControlLayoutVisibility", "$lampType")
    if (lampType == LampType.FIVE_LIGHTS) {
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


@BindingAdapter("workerButtonText")
fun setWorkerButtonText(textView: TextView, isRunning: Boolean){
    if (isRunning) {
        textView.text = textView.context.getString(R.string.background_work_stop)
    } else {
        textView.text = textView.context.getString(R.string.background_work_start)
    }
}


@BindingAdapter("onFiveLightsClickListener")
fun bindOnFiveLightsClickListener(textView: TextView, clickListener: OnFiveLightsClickListener){
    textView.setOnClickListener {
        clickListener.onClick(textView.text.toString())
    }
}

