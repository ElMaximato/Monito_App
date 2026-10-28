package com.monito.mobile.domain.repository

import com.monito.mobile.domain.model.WeatherReport

interface WeatherRepository {
    suspend fun isRaining(): Boolean

    /** Clima de hoy para la pantalla "Mi día". Lanza si no hay conexión o faltan datos. */
    suspend fun getReport(): WeatherReport
}
