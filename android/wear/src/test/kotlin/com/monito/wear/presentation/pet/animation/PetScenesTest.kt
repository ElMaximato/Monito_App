package com.monito.wear.presentation.pet.animation

import com.monito.shared.state.PetState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PetScenesTest {

    private fun count(state: PetState, t: Float, color: Long): Int {
        val f = PetScenes.frame(state, t)!!
        return (f.behind + f.front).count { it.color == color }
    }

    @Test
    fun `el paraguas no esta al inicio, se abre una sola vez y se queda igual`() {
        val umbrella = 0xFFF28C38L
        assertEquals(0, count(PetState.RAIN, 0.1f, umbrella))
        val abierto = count(PetState.RAIN, 2f, umbrella)
        assertTrue(abierto > 0)
        assertEquals(abierto, count(PetState.RAIN, 30f, umbrella))
    }

    @Test
    fun `el abanico sale despues de sudar y se queda`() {
        val fan = 0xFFFF9A3CL
        assertEquals(0, count(PetState.HOT, 1f, fan))
        assertTrue(count(PetState.HOT, 3f, fan) > 0)
        assertTrue(count(PetState.HOT, 40f, fan) > 0)
    }

    @Test
    fun `la bufanda se pone una vez y se queda`() {
        val scarf = 0xFFE8546AL
        assertEquals(0, count(PetState.COLD, 0.5f, scarf))
        assertTrue(count(PetState.COLD, 3f, scarf) > 0)
        assertTrue(count(PetState.COLD, 50f, scarf) > 0)
    }

    @Test
    fun `los nueve estados con escena producen cajas validas durante un minuto`() {
        val estados = listOf(
            PetState.IDLE, PetState.BORED, PetState.NIGHT, PetState.CALM, PetState.EXCITED,
            PetState.PET, PetState.RAIN, PetState.HOT, PetState.COLD,
        )
        for (s in estados) {
            assertTrue(PetScenes.handles(s))
            var t = 0f
            while (t < 60f) {
                val f = PetScenes.frame(s, t)!!
                assertTrue((f.behind + f.erase + f.face + f.front).all { it.w > 0f && it.h > 0f && !it.x.isNaN() && !it.y.isNaN() })
                t += 0.1f
            }
        }
    }

    @Test
    fun `caminar, correr y susto siguen con sus hojas de sprites`() {
        assertFalse(PetScenes.handles(PetState.WALK))
        assertFalse(PetScenes.handles(PetState.RUN))
        assertFalse(PetScenes.handles(PetState.SCARED))
    }
}
