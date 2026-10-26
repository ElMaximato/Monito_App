package com.monito.wear.sensors

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeDetectorTest {

    @Test
    fun `reposo no es sacudida`() {
        assertFalse(ShakeDetector().onSample(0f, 0f, 9.81f, 1_000))
    }

    @Test
    fun `golpe fuerte es sacudida y respeta el enfriamiento`() {
        val detector = ShakeDetector()
        assertTrue(detector.onSample(0f, 0f, 40f, 1_000))
        assertFalse(detector.onSample(0f, 0f, 40f, 1_100))
        assertTrue(detector.onSample(0f, 0f, 40f, 3_000))
    }
}
