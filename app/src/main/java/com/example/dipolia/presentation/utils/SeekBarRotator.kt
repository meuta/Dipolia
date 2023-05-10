package com.example.dipolia.presentation.utils

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

class SeekBarRotator : ViewGroup {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val child = getChildAt(0)
        if (child.visibility != GONE) {
            // swap width and height for child
            measureChild(child, heightMeasureSpec, widthMeasureSpec)
            setMeasuredDimension(
                child.measuredHeightAndState,
                child.measuredWidthAndState
            )
        } else {
            setMeasuredDimension(
                resolveSizeAndState(0, widthMeasureSpec, 0),
                resolveSizeAndState(0, heightMeasureSpec, 0)
            )
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val child = getChildAt(0)
        if (child.visibility != GONE) {
            // rotate the child 90 degrees counterclockwise around its upper-left
            child.pivotX = 0f
            child.pivotY = 0f
            child.rotation = -90f
            // place the child below this view, so it rotates into view
            val mywidth = r - l
            val myheight = b - t
            child.layout(0, myheight, myheight, myheight + mywidth)
        }
    }
}