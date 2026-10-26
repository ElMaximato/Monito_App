package com.monito.wear.sensors

import com.monito.wear.domain.model.MotionType
import kotlin.math.sqrt

/**
 * Distingue quieto / caminando / corriendo por cuánto varía la aceleración en una ventana.
 * Umbrales provisionales: calibrar con un reloj real.
 */
class MotionClassifier(
    private val windowMs: Long = 2000,
    private val walkStdDev: Float = 1.0f,
    private val runStdDev: Float = 4.0f,
    private val minSamples: Int = 10,
) {
    private val samples = ArrayDeque<Pair<Long, Float>>()
    private var current = MotionType.STILL

    /** magnitude = módulo del vector de aceleración en m/s². */
    fun onSample(magnitude: Float, timeMs: Long): MotionType {
        samples.addLast(timeMs to magnitude)
        while (samples.isNotEmpty() && timeMs - samples.first().first > windowMs) samples.removeFirst()
        if (samples.size < minSamples) return current

        val mean = samples.map { it.second }.average().toFloat()
        val variance = samples.map { (it.second - mean) * (it.second - mean) }.average().toFloat()
        val std = sqrt(variance)

        current = when {
            std >= runStdDev -> MotionType.RUN
            std >= walkStdDev -> MotionType.WALK
            else -> MotionType.STILL
        }
        return current
    }
}
