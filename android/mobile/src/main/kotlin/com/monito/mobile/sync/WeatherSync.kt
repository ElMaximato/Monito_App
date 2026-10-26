package com.monito.mobile.sync

import android.util.Log
import com.monito.mobile.config.AppConfig
import com.monito.mobile.domain.repository.WeatherRepository
import com.monito.mobile.watch.WatchMessenger
import com.monito.shared.protocol.DataLayerPaths
import com.monito.shared.protocol.WeatherMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Consulta el clima cada pocos minutos y le avisa al reloj si llueve.
 * Funciona mientras el proceso de la app esté vivo; para segundo plano real
 * se migrará a WorkManager (paso posterior).
 */
class WeatherSync(
    private val weather: WeatherRepository,
    private val messenger: WatchMessenger,
) {
    fun start(scope: CoroutineScope): Job = scope.launch {
        while (isActive) {
            runCatching {
                val raining = weather.isRaining()
                messenger.send(
                    DataLayerPaths.WEATHER,
                    WeatherMessage(raining, System.currentTimeMillis()).toBytes(),
                )
            }.onFailure { Log.w("WeatherSync", "No se pudo consultar el clima", it) }
            delay(AppConfig.WEATHER_REFRESH_MINUTES * 60_000L)
        }
    }
}
