package com.monito.mobile.domain.weather

import com.monito.mobile.domain.model.WeatherReport
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** ALERT = importa ya; WARN = conviene prepararse; INFO = dato útil. El orden del enum es el orden en pantalla. */
enum class TipSeverity { ALERT, WARN, INFO }

data class WeatherTip(val severity: TipSeverity, val text: String)

/**
 * Convierte el clima de hoy en consejos cortos. Lógica pura (sin Android) para probarla en src/test.
 * movingMinutes = minutos en movimiento que lleva el día (null si no se sabe); sirve para animar a salir.
 */
object WeatherAdvisor {

    private val STORM_CODES = 95..99

    fun tipsFor(report: WeatherReport, movingMinutes: Double? = null): List<WeatherTip> {
        val tips = mutableListOf<WeatherTip>()
        val stormy = report.weatherCode in STORM_CODES
        val hottest = max(report.maxC, report.apparentC)
        val coldest = min(report.minC, report.apparentC)

        if (stormy) {
            tips += WeatherTip(TipSeverity.ALERT, "Hay riesgo de tormenta eléctrica. Mejor quédate bajo techo.")
        }

        when {
            report.isRaining ->
                tips += WeatherTip(TipSeverity.WARN, "Está lloviendo ahora. Lleva paraguas o espera a que pase.")
            report.rainChancePct >= 60 ->
                tips += WeatherTip(TipSeverity.WARN, "Hay ${report.rainChancePct}% de probabilidad de lluvia hoy. Lleva paraguas.")
            report.rainChancePct >= 30 ->
                tips += WeatherTip(TipSeverity.INFO, "Hay ${report.rainChancePct}% de probabilidad de lluvia. Un paraguas no estorba.")
        }

        when {
            hottest >= 40 -> tips += WeatherTip(
                TipSeverity.ALERT,
                "Calor extremo (hasta ${hottest.roundToInt()}°). Evita el sol directo, toma agua seguido y descansa a la sombra.",
            )
            hottest >= 35 -> tips += WeatherTip(
                TipSeverity.WARN,
                "Hace mucho calor (hasta ${hottest.roundToInt()}°). Toma agua y busca sombra al mediodía.",
            )
            hottest >= 30 -> tips += WeatherTip(
                TipSeverity.INFO,
                "Día caluroso (hasta ${hottest.roundToInt()}°). Acuérdate de hidratarte.",
            )
        }

        when {
            coldest <= 5 -> tips += WeatherTip(
                TipSeverity.WARN,
                "Hace mucho frío (hasta ${coldest.roundToInt()}°). Abrígate bien.",
            )
            coldest <= 12 -> tips += WeatherTip(
                TipSeverity.INFO,
                "Está fresco (mínima de ${coldest.roundToInt()}°). Lleva una chamarra.",
            )
        }

        when {
            report.uvMax >= 11 -> tips += WeatherTip(
                TipSeverity.ALERT,
                "Índice UV extremo (${report.uvMax.roundToInt()}). Usa bloqueador y gorra, y evita el sol del mediodía.",
            )
            report.uvMax >= 8 -> tips += WeatherTip(
                TipSeverity.WARN,
                "Índice UV muy alto (${report.uvMax.roundToInt()}). Usa bloqueador y gorra.",
            )
            report.uvMax >= 6 -> tips += WeatherTip(
                TipSeverity.INFO,
                "Índice UV alto (${report.uvMax.roundToInt()}). Bloqueador si vas a estar al sol.",
            )
        }

        when {
            report.windKmh >= 50 -> tips += WeatherTip(
                TipSeverity.WARN,
                "Viento muy fuerte (${report.windKmh.roundToInt()} km/h). Cuidado con el polvo y los objetos sueltos.",
            )
            report.windKmh >= 35 -> tips += WeatherTip(
                TipSeverity.INFO,
                "Viento fuerte (${report.windKmh.roundToInt()} km/h).",
            )
        }

        val pleasant = !stormy && !report.isRaining && report.rainChancePct < 30 && hottest < 30 && coldest > 12
        if (pleasant && movingMinutes != null && movingMinutes < 30) {
            tips += WeatherTip(TipSeverity.INFO, "Buen clima y poco movimiento por ahora: ideal para salir a caminar.")
        }

        if (tips.isEmpty()) {
            tips += WeatherTip(TipSeverity.INFO, "Clima tranquilo hoy. Buen día para salir.")
        }
        return tips.sortedBy { it.severity.ordinal }
    }
}
