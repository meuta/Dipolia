package com.example.dipolia.presentation

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.dipolia.R


private const val TAG = "BindingAdapters"

@BindingAdapter("dipolLabelColor")
fun bindDipolLabelColor(view: View, colorList: List<Double>?) {
//    Log.d(TAG, "bindDipolLabelColor: colorlist = $colorList ")
    val list = colorList ?: listOf(0.0, 0.0, 0.0)
    view.setBackgroundColor(Color.parseColor(colorToUI(list)))
}

@BindingAdapter("fiveLightsLabelColor")
fun bindFiveLightsLabelColor(view: View, colorList: List<Double>?) {
//    Log.d(TAG, "bindFiveLightsLabelColor: colorlist = $colorList")
    val list = colorList ?: listOf(0.0, 0.0, 0.0, 0.0, 0.0)
    view.setBackgroundColor(Color.parseColor(colorFiveLightsToUI(list)))
}

private fun colorFiveLightsToUI(colorList: List<Double>): String {

    val string = String.format(
        "#" + "%02x%02x%02x",
        (colorList[0] * 255 * 2 / 3 + colorList[3] * 255 / 3).toInt(),
        (colorList[1] * 255 * 2 / 3 + colorList[3] * 255 / 3).toInt(),
        (colorList[2] * 255 * 2 / 3 + colorList[3] * 255 / 3).toInt()
    )
//    Log.d("bindFiveLightsLabelColor", "colorToUI $string")
    return string
}

private fun colorToUI(colorList: List<Double>): String {

    val string = String.format(
        "#" + "%02x%02x%02x",
        (colorList[0] * 255).toInt(),
        (colorList[1] * 255).toInt(),
        (colorList[2] * 255).toInt()
    )
//    Log.d("bindLabelColor", "colorToUI $string")
    return string
}

@BindingAdapter("selectedBackground")
fun setSelectedBackground(textView: TextView, isSelected: Boolean) {
//    Log.d(TAG, "setSelectedBackground: isSelected = $isSelected")
    textView.setBackgroundColor(ContextCompat.getColor(
        textView.context,
        if (isSelected) R.color.colorSelect else R.color.colorPrimaryDark
    ))
}

@BindingAdapter("selectedVisibility")
fun setSelectedVisibility(view: View, isSelected: Boolean) {
//    Log.d(TAG, "setSelectedVisibility: isSelected = $isSelected")
    view.visibility = if (isSelected) VISIBLE else INVISIBLE
}


@BindingAdapter("workerButtonText")
fun setWorkerButtonText(textView: TextView, isRunning: Boolean) {
//    Log.d(TAG, "setWorkerButtonText: isRunning = $isRunning ")
    textView.text = if (isRunning) textView.context.getString(R.string.background_work_stop) else textView.context.getString(R.string.background_work_start)
}


@BindingAdapter("etLoopSecondsText")
fun setEtLoopSecondsText(editText: EditText, seconds: Double?) {
    editText.setText(seconds.toString())
}
