package com.monito.shared

import com.monito.shared.state.PetState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PetStateTest {
    @Test
    fun `codigo desconocido cae en IDLE`() {
        assertEquals(PetState.IDLE, PetState.fromCode("NO_EXISTE"))
        assertEquals(PetState.IDLE, PetState.fromCode(null))
    }

    @Test
    fun `la sacudida siempre tiene la mayor prioridad`() {
        val best = PetState.entries.minBy { it.priority }
        assertEquals(PetState.SCARED, best)
        assertTrue(PetState.entries.all { it.priority in 1..8 })
    }
}
