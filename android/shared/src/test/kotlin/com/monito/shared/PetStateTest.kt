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
    fun `la caricia siempre tiene la mayor prioridad y dormir va justo despues`() {
        val sorted = PetState.entries.sortedBy { it.priority }
        assertEquals(PetState.PET, sorted[0])
        assertEquals(PetState.NIGHT, sorted[1])
        assertTrue(PetState.PET.priority < PetState.SCARED.priority)
        assertTrue(PetState.entries.all { it.priority in 1..8 })
    }
}
