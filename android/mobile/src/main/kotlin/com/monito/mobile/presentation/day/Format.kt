package com.monito.mobile.presentation.day

import java.text.NumberFormat
import kotlin.math.roundToInt

/** 42 → "42 min", 60 → "1 h", 125 → "2 h 05 min". */
fun formatDuration(minutes: Double): String {
    val total = minutes.roundToInt().coerceAtLeast(0)
    val hours = total / 60
    val rest = total % 60
    return when {
        hours == 0 -> "$total min"
        rest == 0 -> "$hours h"
        else -> "$hours h ${rest.toString().padStart(2, '0')} min"
    }
}

fun formatSteps(steps: Int): String = NumberFormat.getIntegerInstance().format(steps)
