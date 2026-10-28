package com.monito.mobile.domain.weather

import com.monito.mobile.domain.model.WeatherReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherAdvisorTest {

    /** Un día templado y sin nada especial; cada prueba cambia solo lo que le interesa. */
    private fun report(
        temperatureC: Double = 22.0,
        apparentC: Double = 22.0,
        isRaining: Boolean = false,
        rainChancePct: Int = 0,
        maxC: Double = 26.0,
        minC: Double = 16.0,
        uvMax: Double = 3.0,
        windKmh: Double = 10.0,
        weatherCode: Int = 0,
    ) = WeatherReport(temperatureC, apparentC, isRaining, rainChancePct, maxC, minC, uvMax, windKmh, weatherCode)

    private fun List<WeatherTip>.anyContains(text: String) = any { it.text.contains(text) }

    @Test
    fun `un dia tranquilo da un solo consejo informativo`() {
        val tips = WeatherAdvisor.tipsFor(report())
        assertEquals(1, tips.size)
        assertEquals(TipSeverity.INFO, tips.first().severity)
        assertTrue(tips.first().text.contains("tranquilo"))
    }

    @Test
    fun `buen clima y poco movimiento invita a caminar`() {
        assertTrue(WeatherAdvisor.tipsFor(report(), movingMinutes = 10.0).anyContains("caminar"))
        assertFalse(WeatherAdvisor.tipsFor(report(), movingMinutes = 45.0).anyContains("caminar"))
        assertFalse(WeatherAdvisor.tipsFor(report(), movingMinutes = null).anyContains("caminar"))
    }

    @Test
    fun `con lluvia o calor no invita a caminar`() {
        assertFalse(WeatherAdvisor.tipsFor(report(isRaining = true), movingMinutes = 0.0).anyContains("caminar"))
        assertFalse(WeatherAdvisor.tipsFor(report(maxC = 33.0), movingMinutes = 0.0).anyContains("caminar"))
    }

    @Test
    fun `lluvia ahora o probable pide paraguas`() {
        val now = WeatherAdvisor.tipsFor(report(isRaining = true))
        assertEquals(TipSeverity.WARN, now.first().severity)
        assertTrue(now.first().text.contains("lloviendo"))

        val likely = WeatherAdvisor.tipsFor(report(rainChancePct = 70)).first()
        assertEquals(TipSeverity.WARN, likely.severity)
        assertTrue(likely.text.contains("70%"))

        val maybe = WeatherAdvisor.tipsFor(report(rainChancePct = 40)).first()
        assertEquals(TipSeverity.INFO, maybe.severity)
    }

    @Test
    fun `el calor sube de nivel con la temperatura`() {
        assertEquals(TipSeverity.INFO, WeatherAdvisor.tipsFor(report(maxC = 31.0)).first().severity)
        assertEquals(TipSeverity.WARN, WeatherAdvisor.tipsFor(report(maxC = 36.0)).first().severity)
        assertEquals(TipSeverity.ALERT, WeatherAdvisor.tipsFor(report(maxC = 42.0)).first().severity)
    }

    @Test
    fun `la sensacion termica tambien cuenta para el calor`() {
        val tips = WeatherAdvisor.tipsFor(report(maxC = 33.0, apparentC = 38.0))
        assertTrue(tips.anyContains("38°"))
    }

    @Test
    fun `frio y fresco piden abrigo`() {
        assertEquals(TipSeverity.WARN, WeatherAdvisor.tipsFor(report(minC = 3.0, apparentC = 5.0)).first().severity)
        assertTrue(WeatherAdvisor.tipsFor(report(minC = 10.0)).anyContains("chamarra"))
    }

    @Test
    fun `uv y viento fuertes se avisan`() {
        assertEquals(TipSeverity.WARN, WeatherAdvisor.tipsFor(report(uvMax = 9.0)).first().severity)
        assertEquals(TipSeverity.ALERT, WeatherAdvisor.tipsFor(report(uvMax = 12.0)).first().severity)
        assertTrue(WeatherAdvisor.tipsFor(report(windKmh = 40.0)).anyContains("km/h"))
    }

    @Test
    fun `la tormenta es alerta y va primero`() {
        val tips = WeatherAdvisor.tipsFor(report(weatherCode = 95, isRaining = true), movingMinutes = 0.0)
        assertEquals(TipSeverity.ALERT, tips.first().severity)
        assertTrue(tips.first().text.contains("tormenta"))
        assertFalse(tips.anyContains("caminar"))
    }

    @Test
    fun `los consejos salen de mayor a menor gravedad`() {
        val tips = WeatherAdvisor.tipsFor(report(maxC = 42.0, rainChancePct = 40, uvMax = 7.0, windKmh = 60.0))
        assertEquals(tips.sortedBy { it.severity.ordinal }, tips)
        assertEquals(TipSeverity.ALERT, tips.first().severity)
        assertEquals(TipSeverity.INFO, tips.last().severity)
    }
}
