package com.example.dipolia.presentation.utils

import android.content.Context
import android.util.AttributeSet

class SeekbarWithIndex(context: Context, attrs: AttributeSet) :
    androidx.appcompat.widget.AppCompatSeekBar(context, attrs) {
    var index: Int = -1
}