package com.monito.mobile.data.remote.api

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
}
