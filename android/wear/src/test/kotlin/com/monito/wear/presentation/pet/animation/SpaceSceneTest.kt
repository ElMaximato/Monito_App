package com.monito.wear.presentation.pet.animation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpaceSceneTest {

    private fun frame(t: Float) = SpaceScene.boxes(t, -80f, 470f, -30f, 660f)

    @Test
    fun `hay estrellas, suelo de luna y todo se dibuja con tamano valido`() {
        val boxes = frame(1f)
        assertTrue(boxes.size > 60)
        assertTrue(boxes.all { it.w > 0f && it.h > 0f && !it.x.isNaN() && !it.y.isNaN() })
        assertTrue(boxes.any { it.y >= SpaceScene.GROUND_TOP })
    }

    @Test
    fun `el fondo se mueve con el tiempo`() {
        val a = frame(1f).map { listOf(it.x, it.y, it.color.toFloat()) }
        val b = frame(9f).map { listOf(it.x, it.y, it.color.toFloat()) }
        assertNotEquals(a, b)
    }

    @Test
    fun `el suelo es mas alto en el centro que a los lados`() {
        assertEquals(SpaceScene.GROUND_TOP, SpaceScene.groundTop(195f), 0.01f)
        assertTrue(SpaceScene.groundTop(-60f) > SpaceScene.groundTop(195f))
    }
}
