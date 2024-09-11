package com.example.ticketvalidation

import android.content.res.Resources
import android.util.TypedValue
import kotlin.math.roundToInt

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).roundToInt()