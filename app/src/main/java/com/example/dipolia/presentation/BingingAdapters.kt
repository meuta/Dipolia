package com.example.dipolia.presentation

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.dipolia.R


@BindingAdapter("lableColor")
fun bindLableColor(view: View, colorList: List<Double>?) {
    Log.d("View", "colorlist $colorList $view ")
    val list = colorList ?: listOf(0.0, 0.0, 0.0)
//    colorList?.let { view.setBackgroundColor(Color.parseColor(colorToUI(colorList))) }
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
        view.visibility = GONE
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