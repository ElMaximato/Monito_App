package com.monito.mobile.data.remote

import com.monito.mobile.BuildConfig
import com.monito.mobile.config.AppConfig
import com.monito.mobile.data.remote.api.MonitoApi
import com.monito.mobile.data.remote.api.WeatherApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val monitoApi: MonitoApi by lazy { retrofit(AppConfig.BACKEND_URL).create(MonitoApi::class.java) }
    val weatherApi: WeatherApi by lazy { retrofit(AppConfig.WEATHER_BASE_URL).create(WeatherApi::class.java) }
}
