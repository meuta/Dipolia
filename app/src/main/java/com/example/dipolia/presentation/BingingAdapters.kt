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
import com.example.dipolia.domain.entities.LampDomainEntity


private const val TAG = "BindingAdapters"

@BindingAdapter("dipolLabelColor")
fun bindDipolLabelColor(view: View, colorList: List<Double>?) {
    Log.d(TAG, "bindDipolLabelColor: colorlist = $colorList ")
    val list = colorList ?: listOf(0.0, 0.0, 0.0)
    view.setBackgroundColor(Color.parseColor(colorToUI(list)))
}

@BindingAdapter("fiveLightsLabelColor")
fun bindFiveLightsLabelColor(view: View, colorList: List<Double>?) {
    Log.d(TAG, "bindFiveLightsLabelColor: colorlist = $colorList")
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
//    Log.d("setSelectedBackground", "$isSelected")
    if (isSelected) {
        textView.setBackgroundColor(ContextCompat.getColor(textView.context, R.color.colorSelect))
    } else {
        textView.setBackgroundColor(
            ContextCompat.getColor(
                textView.context,
                R.color.colorPrimaryDark
            )
        )
    }
}

@BindingAdapter("selectedVisibility")
fun setSelectedVisibility(view: View, isSelected: Boolean) {
    if (isSelected) {
        view.visibility = VISIBLE
    } else {
        view.visibility = INVISIBLE
    }
}



@BindingAdapter("pleaseSelectTextViewVisibility")
fun setPleaseSelectTextViewVisibility(view: View, visible: Boolean) {
//    Log.d(TAG, "setPleaseSelectTextViewVisibility: visible = $visible")
    if (visible) {view.visibility = VISIBLE
    } else {
        view.visibility = INVISIBLE
    }
}

@BindingAdapter("dipolControlLayoutVisibility")
fun setDipolControlLayoutVisibility(view: View, visible: Boolean) {
//    Log.d(TAG, "setDipolControlLayoutVisibility: visible = $visible")
    if (visible) {
        view.visibility = VISIBLE
    } else {
        view.visibility = INVISIBLE
    }
}

@BindingAdapter("fiveLightsControlLayoutVisibility")
fun setFiveLightsControlLayoutVisibility(view: View, visible: Boolean) {
//    Log.d(TAG, "setFiveLightsControlLayoutVisibility: visible = $visible")
    if (visible) {
        view.visibility = VISIBLE
    } else {
        view.visibility = INVISIBLE
    }
}


@BindingAdapter("workerButtonText")
fun setWorkerButtonText(textView: TextView, isRunning: Boolean) {
    if (isRunning) {
        textView.text = textView.context.getString(R.string.background_work_stop)
    } else {
        textView.text = textView.context.getString(R.string.background_work_start)
    }
}


@BindingAdapter("etLoopSecondsText")
fun setEtLoopSecondsText(editText: EditText, seconds: Double) {
    editText.setText(seconds.toString())

}


@BindingAdapter("recyclerViewsDividerVisibility")
fun setRecyclerViewsDividerVisibility(view: View, list: List<LampDomainEntity>?) {
    view.visibility = if (list?.filter { it.connected }.isNullOrEmpty()) INVISIBLE else VISIBLE
}