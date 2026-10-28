package com.monito.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Respuesta de Open-Meteo con ?current=...&daily=...&forecast_days=1 */
data class ForecastResponse(val current: ForecastCurrentDto?, val daily: ForecastDailyDto?)

data class ForecastCurrentDto(
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    val precipitation: Double?,
    val rain: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
)

/** Con forecast_days=1 cada lista trae un solo valor (el de hoy). */
data class ForecastDailyDto(
    @SerializedName("temperature_2m_max") val maxTemperature: List<Double?>?,
    @SerializedName("temperature_2m_min") val minTemperature: List<Double?>?,
    @SerializedName("precipitation_probability_max") val rainChance: List<Double?>?,
    @SerializedName("uv_index_max") val uvMax: List<Double?>?,
)
