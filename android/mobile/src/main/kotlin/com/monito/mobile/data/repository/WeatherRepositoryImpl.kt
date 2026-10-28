package com.monito.mobile.data.repository

import com.monito.mobile.config.AppConfig
import com.monito.mobile.data.remote.api.WeatherApi
import com.monito.mobile.domain.model.WeatherReport
import com.monito.mobile.domain.repository.WeatherRepository
import kotlin.math.roundToInt

class WeatherRepositoryImpl(private val api: WeatherApi) : WeatherRepository {

    override suspend fun isRaining(): Boolean {
        val current = api.currentWeather(AppConfig.WEATHER_LATITUDE, AppConfig.WEATHER_LONGITUDE).current
        return (current?.rain ?: 0.0) > 0.0 || (current?.precipitation ?: 0.0) > 0.0
    }

    override suspend fun getReport(): WeatherReport {
        val response = api.forecast(AppConfig.WEATHER_LATITUDE, AppConfig.WEATHER_LONGITUDE)
        val current = response.current ?: error("Open-Meteo no devolvió el clima actual")
        val now = current.temperature ?: error("Open-Meteo no devolvió la temperatura")
        val daily = response.daily
        return WeatherReport(
            temperatureC = now,
            apparentC = current.apparentTemperature ?: now,
            isRaining = (current.rain ?: 0.0) > 0.0 || (current.precipitation ?: 0.0) > 0.0,
            rainChancePct = daily?.rainChance?.firstOrNull()?.roundToInt() ?: 0,
            maxC = daily?.maxTemperature?.firstOrNull() ?: now,
            minC = daily?.minTemperature?.firstOrNull() ?: now,
            uvMax = daily?.uvMax?.firstOrNull() ?: 0.0,
            windKmh = current.windSpeed ?: 0.0,
            weatherCode = current.weatherCode ?: 0,
        )
    }
}
