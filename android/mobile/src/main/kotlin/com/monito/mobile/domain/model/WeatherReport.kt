package com.monito.mobile.domain.model

/** Clima de hoy en la ubicación configurada (Open-Meteo). Temperaturas en °C, viento en km/h. */
data class WeatherReport(
    val temperatureC: Double,
    val apparentC: Double,
    val isRaining: Boolean,
    val rainChancePct: Int,
    val maxC: Double,
    val minC: Double,
    val uvMax: Double,
    val windKmh: Double,
    val weatherCode: Int,
)
