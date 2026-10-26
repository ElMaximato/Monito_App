package com.monito.mobile.data.remote.dto

/** Respuesta de Open-Meteo con ?current=precipitation,rain */
data class WeatherResponse(val current: CurrentWeatherDto?)

data class CurrentWeatherDto(val precipitation: Double?, val rain: Double?)
