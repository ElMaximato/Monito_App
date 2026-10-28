package com.monito.mobile.data.remote.api

import com.monito.mobile.data.remote.dto.ForecastResponse
import com.monito.mobile.data.remote.dto.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** Open-Meteo: gratis y sin API key. */
interface WeatherApi {
    @GET("v1/forecast")
    suspend fun currentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "precipitation,rain",
    ): WeatherResponse

    /** Clima de hoy: valores actuales + resumen del día (máx/mín, probabilidad de lluvia, UV). */
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,apparent_temperature,precipitation,rain,weather_code,wind_speed_10m",
        @Query("daily") daily: String =
            "temperature_2m_max,temperature_2m_min,precipitation_probability_max,uv_index_max",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 1,
    ): ForecastResponse
}
