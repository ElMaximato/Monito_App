package com.monito.wear.domain.model

/** Foto de todo lo que el monito "siente" en un momento dado. */
data class SensorInputs(
    val temperatureC: Float? = null,
    val heartRate: Int? = null,
    val motion: MotionType = MotionType.STILL,
    val climbing: Boolean = false,
    val isRaining: Boolean = false,
    val shakeActive: Boolean = false,
    val petActive: Boolean = false,
    val hourOfDay: Int = 12,
    val secondsStill: Long = 0,
)
