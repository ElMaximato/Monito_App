package com.monito.wear.sensors

import kotlin.math.sqrt

/** Detecta una sacudida fuerte de la muñeca a partir del acelerómetro. */
class ShakeDetector(
    private val thresholdG: Float = 2.5f,
    private val cooldownMs: Long = 1500,
) {
    private var lastShakeAt = Long.MIN_VALUE / 2

    /** x, y, z en m/s² (incluye la gravedad). Devuelve true si cuenta como una sacudida nueva. */
    fun onSample(x: Float, y: Float, z: Float, timeMs: Long): Boolean {
        val g = sqrt(x * x + y * y + z * z) / GRAVITY
        if (g < thresholdG || timeMs - lastShakeAt < cooldownMs) return false
        lastShakeAt = timeMs
        return true
    }

    private companion object {
        const val GRAVITY = 9.80665f
    }
}
