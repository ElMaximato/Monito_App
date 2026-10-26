package com.monito.mobile.domain.repository

interface WeatherRepository {
    suspend fun isRaining(): Boolean
}
