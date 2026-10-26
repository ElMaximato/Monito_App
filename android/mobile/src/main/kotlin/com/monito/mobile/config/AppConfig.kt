package com.monito.mobile.config

import com.monito.mobile.BuildConfig

/** Acceso tipado a los valores definidos en gradle.properties. */
object AppConfig {
    const val BACKEND_URL: String = BuildConfig.BACKEND_URL
    const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
    const val WEATHER_LATITUDE: Double = BuildConfig.WEATHER_LATITUDE
    const val WEATHER_LONGITUDE: Double = BuildConfig.WEATHER_LONGITUDE
    const val WEATHER_REFRESH_MINUTES = 15L
    const val DEFAULT_PET_NAME = "Monito"
}
