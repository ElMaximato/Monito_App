package com.monito.wear.domain.rules

/**
 * Umbrales PROVISIONALES. Se calibran con un reloj real o en el emulador;
 * más adelante podrán venir de ajustes del móvil.
 */
data class Thresholds(
    val hotC: Float = 30f,
    val coldC: Float = 15f,
    val highHeartRate: Int = 110,
    val lowHeartRate: Int = 65,
    val nightStartHour: Int = 21,
    val nightEndHour: Int = 6,
    val boredAfterSeconds: Long = 300,
)
