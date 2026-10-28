package com.monito.mobile.presentation.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monito.mobile.domain.model.DailySummary
import com.monito.mobile.domain.model.WeatherReport
import com.monito.mobile.domain.usecase.GetDailySummaryUseCase
import com.monito.mobile.domain.usecase.GetWeatherReportUseCase
import com.monito.mobile.domain.weather.WeatherAdvisor
import com.monito.mobile.domain.weather.WeatherTip
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class DayUiState(
    val loading: Boolean = true,
    val summary: DailySummary? = null,
    val summaryError: Boolean = false,
    val weather: WeatherReport? = null,
    val weatherError: Boolean = false,
    val tips: List<WeatherTip> = emptyList(),
)

/**
 * "Mi día": resumen de lo que registró el reloj hoy + clima y consejos.
 * El resumen y el clima se piden a la vez y fallan por separado (si el backend está apagado,
 * el clima se sigue viendo, y al revés).
 */
class DayViewModel(
    private val getDailySummary: GetDailySummaryUseCase,
    private val getWeatherReport: GetWeatherReportUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DayUiState())
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            // al actualizar se conserva lo que ya se veía; solo el primer intento muestra el cargando
            _uiState.update { it.copy(loading = it.summary == null && it.weather == null) }
            val (from, to) = todayRange()
            val summaryJob = async { runCatching { getDailySummary(from, to) } }
            val weatherJob = async { runCatching { getWeatherReport() } }
            val summary = summaryJob.await()
            val weather = weatherJob.await()

            _uiState.update { old ->
                val newSummary = summary.getOrNull() ?: old.summary
                val newWeather = weather.getOrNull() ?: old.weather
                old.copy(
                    loading = false,
                    summary = newSummary,
                    summaryError = summary.isFailure,
                    weather = newWeather,
                    weatherError = weather.isFailure,
                    tips = newWeather?.let { WeatherAdvisor.tipsFor(it, newSummary?.movingMinutes) }.orEmpty(),
                )
            }
        }
    }

    /** El día local completo (de las 00:00 a las 00:00 siguientes) en epoch ms. */
    private fun todayRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone)
        return start.toInstant().toEpochMilli() to start.plusDays(1).toInstant().toEpochMilli()
    }
}
