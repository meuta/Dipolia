package com.example.dipolia.presentation

import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.databinding.BindingAdapter


@BindingAdapter("lableColor")
fun bindLableColor(view: View, colorList: List<Double>?) {
    Log.d("View", "colorlist$colorList $view ")
    colorList?.let { view.setBackgroundColor(Color.parseColor(colorToUI(colorList))) }
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