package com.monito.wear.sensors

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClimbDetectorTest {

    @Test
    fun `presion estable no es escalera`() {
        val detector = ClimbDetector()
        detector.onSample(1013.0f, 0)
        detector.onSample(1013.0f, 5_000)
        assertFalse(detector.isClimbing(5_000))
    }

    @Test
    fun `bajar la presion rapido es escalera y deja de serlo al pasar la ventana`() {
        val detector = ClimbDetector()
        detector.onSample(1013.00f, 0)
        detector.onSample(1012.80f, 4_000)
        assertTrue(detector.isClimbing(4_000))
        assertFalse(detector.isClimbing(30_000))
    }
}
