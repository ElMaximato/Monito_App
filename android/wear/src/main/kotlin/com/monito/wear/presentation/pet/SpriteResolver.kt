package com.monito.wear.presentation.pet

import androidx.annotation.DrawableRes
import com.monito.shared.state.PetState
import com.monito.wear.R

/**
 * Traduce un estado al PNG que lo representa. Si el estado aún no tiene sprite
 * (por ejemplo CLIMB), usa el normal.
 */
@DrawableRes
fun spriteResFor(state: PetState): Int = when (state.spriteName) {
    "astro_idle" -> R.drawable.astro_idle
    "astro_scared" -> R.drawable.astro_scared
    "astro_excited" -> R.drawable.astro_excited
    "astro_sleep" -> R.drawable.astro_sleep
    "astro_rain" -> R.drawable.astro_rain
    "astro_hot" -> R.drawable.astro_hot
    "astro_cold" -> R.drawable.astro_cold
    "astro_sit" -> R.drawable.astro_sit
    else -> R.drawable.astro_idle
}
