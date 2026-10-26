package com.monito.wear.sensors

/**
 * Subir escaleras = la presión baja (≈ 0.12 hPa por metro de altura).
 * Si baja al menos [dropHpa] dentro de [windowMs], se considera que está trepando.
 */
class ClimbDetector(
    private val windowMs: Long = 10_000,
    private val dropHpa: Float = 0.10f,
) {
    private val samples = ArrayDeque<Pair<Long, Float>>()

    fun onSample(hpa: Float, timeMs: Long) {
        samples.addLast(timeMs to hpa)
        trim(timeMs)
    }

    /** Se consulta con el reloj del sistema, porque el barómetro solo reporta cuando cambia. */
    fun isClimbing(nowMs: Long): Boolean {
        trim(nowMs)
        if (samples.size < 2) return false
        return samples.first().second - samples.last().second >= dropHpa
    }

    private fun trim(nowMs: Long) {
        while (samples.isNotEmpty() && nowMs - samples.first().first > windowMs) samples.removeFirst()
    }
}
