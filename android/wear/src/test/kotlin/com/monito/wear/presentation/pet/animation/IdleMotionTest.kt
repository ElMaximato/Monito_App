package com.monito.wear.presentation.pet.animation

import org.junit.Assert.assertEquals
import org.junit.Test

class IdleMotionTest {

    @Test
    fun `parpadea al inicio de cada ciclo y luego tiene los ojos abiertos`() {
        assertEquals(EyeStyle.CLOSED, IdleMotion.frame(0.05f).eyes)
        assertEquals(EyeStyle.OPEN, IdleMotion.frame(1f).eyes)
    }

    @Test
    fun `mira a la izquierda, luego a la derecha y vuelve al centro`() {
        assertEquals(-IdleMotion.GLANCE, IdleMotion.frame(3.5f).glanceX, 0.01f)
        assertEquals(IdleMotion.GLANCE, IdleMotion.frame(7f).glanceX, 0.01f)
        assertEquals(0f, IdleMotion.frame(1f).glanceX, 0.01f)
    }

    @Test
    fun `respira bajando y subiendo el cuerpo`() {
        assertEquals(IdleMotion.STEP, IdleMotion.frame(1f).bobY, 0.01f)
        assertEquals(0f, IdleMotion.frame(3f).bobY, 0.01f)
    }

    @Test
    fun `de vez en cuando se alegra y da un saltito`() {
        val jumping = IdleMotion.frame(9.3f)
        assertEquals(EyeStyle.HAPPY, jumping.eyes)
        assertEquals(-14f, jumping.hopY, 0.1f)

        val landed = IdleMotion.frame(9.9f)
        assertEquals(EyeStyle.HAPPY, landed.eyes)
        assertEquals(0f, landed.hopY, 0.01f)
    }
}
