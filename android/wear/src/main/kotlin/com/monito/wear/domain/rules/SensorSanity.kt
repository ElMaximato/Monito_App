package com.monito.wear.domain.rules

/**
 * Descarta lecturas imposibles antes de decidir el estado. El emulador a veces manda basura
 * (por ejemplo una temperatura de 1.5e21 °C) y eso dejaba al monito con frío o calor para siempre.
 */
object SensorSanity {
    // Margen amplio a propósito: el emulador deja probar de -40 a 100 °C y solo se descarta la basura (1.5e21).
    val TEMPERATURE_C = -90f..150f
    val HEART_RATE_BPM = 30..220

    fun temperature(value: Float?): Float? = value?.takeIf { it.isFinite() && it in TEMPERATURE_C }

    fun heartRate(value: Int?): Int? = value?.takeIf { it in HEART_RATE_BPM }
}
