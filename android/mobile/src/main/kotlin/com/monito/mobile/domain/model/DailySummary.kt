package com.monito.mobile.domain.model

data class HeartRateStats(val avg: Int, val min: Int, val max: Int, val samples: Int)

/** Lo que registró el reloj en el día local: minutos por tipo de actividad, pasos y pulso. */
data class DailySummary(
    val trackedMinutes: Double,
    val movingMinutes: Double,
    val restingMinutes: Double,
    val sleepMinutes: Double,
    val steps: Int,
    val heartRate: HeartRateStats?,
) {
    val hasData: Boolean get() = trackedMinutes > 0.0 || steps > 0
}
