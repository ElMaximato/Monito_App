package com.monito.wear.presentation.pet.animation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SpriteAnimationTest {

    // 3 cuadros de introducción + 4 de bucle, a 10 cuadros por segundo
    private val animation = SpriteAnimation(
        sheetRes = 0, frameWidth = 10, frameHeight = 10, columns = 3,
        introFrames = 3, loopFrames = 4, fps = 10f,
        stageX = 0f, stageY = 0f, stageWidth = 10f, stageHeight = 10f,
    )

    private fun ms(value: Long) = value * 1_000_000L

    @Test
    fun `la introduccion se reproduce una sola vez`() {
        assertEquals(0, animation.frameAt(ms(0)))
        assertEquals(1, animation.frameAt(ms(100)))
        assertEquals(2, animation.frameAt(ms(250)))
    }

    @Test
    fun `despues de la introduccion repite solo el bucle`() {
        assertEquals(3, animation.frameAt(ms(300)))
        assertEquals(6, animation.frameAt(ms(650)))
        assertEquals(3, animation.frameAt(ms(700))) // vuelve al primer cuadro del bucle, no al 0
        assertEquals(4, animation.frameAt(ms(10_000))) // tick 100 -> 3 + (97 % 4)
    }

    @Test
    fun `tiempo negativo se trata como cero`() {
        assertEquals(0, animation.frameAt(-5L))
    }

    @Test
    fun `posicion en la hoja por columnas y filas`() {
        assertEquals(0, animation.column(0)); assertEquals(0, animation.row(0))
        assertEquals(1, animation.column(4)); assertEquals(1, animation.row(4))
        assertEquals(0, animation.column(6)); assertEquals(2, animation.row(6))
    }

    @Test
    fun `sin cuadros de bucle no es valida`() {
        assertThrows(IllegalArgumentException::class.java) {
            animation.copy(loopFrames = 0)
        }
    }
}
