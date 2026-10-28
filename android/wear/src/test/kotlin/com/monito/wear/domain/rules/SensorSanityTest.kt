package com.monito.wear.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SensorSanityTest {

    @Test
    fun `una temperatura razonable pasa tal cual`() {
        assertEquals(22.5f, SensorSanity.temperature(22.5f))
        assertEquals(-5f, SensorSanity.temperature(-5f))
    }

    @Test
    fun `los extremos que se prueban en el emulador pasan`() {
        assertEquals(100f, SensorSanity.temperature(100f))
        assertEquals(-40f, SensorSanity.temperature(-40f))
    }

    @Test
    fun `una temperatura imposible se descarta`() {
        assertNull(SensorSanity.temperature(1.5047462e21f))
        assertNull(SensorSanity.temperature(-1.5047462e21f))
        assertNull(SensorSanity.temperature(500f))
        assertNull(SensorSanity.temperature(Float.NaN))
        assertNull(SensorSanity.temperature(null))
    }

    @Test
    fun `el pulso fuera de rango humano se descarta`() {
        assertEquals(72, SensorSanity.heartRate(72))
        assertNull(SensorSanity.heartRate(0))
        assertNull(SensorSanity.heartRate(400))
        assertNull(SensorSanity.heartRate(null))
    }
}
