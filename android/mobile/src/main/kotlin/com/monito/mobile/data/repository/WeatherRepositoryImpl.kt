package com.monito.mobile.data.repository

import com.monito.mobile.config.AppConfig
import com.monito.mobile.data.remote.api.WeatherApi
import com.monito.mobile.domain.repository.WeatherRepository

class WeatherRepositoryImpl(private val api: WeatherApi) : WeatherRepository {

    override suspend fun isRaining(): Boolean {
        val current = api.currentWeather(AppConfig.WEATHER_LATITUDE, AppConfig.WEATHER_LONGITUDE).current
        return (current?.rain ?: 0.0) > 0.0 || (current?.precipitation ?: 0.0) > 0.0
    }
}
