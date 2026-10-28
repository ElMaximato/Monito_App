package com.monito.mobile.presentation.day

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {

    @Test
    fun `menos de una hora se muestra en minutos`() {
        assertEquals("0 min", formatDuration(0.0))
        assertEquals("42 min", formatDuration(42.4))
        assertEquals("59 min", formatDuration(59.0))
    }

    @Test
    fun `una hora o mas se muestra con horas`() {
        assertEquals("1 h", formatDuration(60.0))
        assertEquals("2 h 05 min", formatDuration(125.0))
        assertEquals("1 h 30 min", formatDuration(89.6))
    }

    @Test
    fun `un valor negativo no se muestra raro`() {
        assertEquals("0 min", formatDuration(-3.0))
    }
}
