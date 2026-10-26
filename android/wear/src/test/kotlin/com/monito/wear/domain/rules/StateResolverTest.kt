package com.monito.wear.domain.rules

import com.monito.shared.state.PetState
import com.monito.wear.domain.model.MotionType
import com.monito.wear.domain.model.SensorInputs
import org.junit.Assert.assertEquals
import org.junit.Test

class StateResolverTest {

    @Test
    fun `sin nada activo es IDLE`() {
        assertEquals(PetState.IDLE, StateResolver.resolve(SensorInputs()))
    }

    @Test
    fun `la sacudida gana sobre todo lo demas`() {
        val inputs = SensorInputs(
            shakeActive = true, petActive = true, isRaining = true,
            motion = MotionType.RUN, temperatureC = 40f, hourOfDay = 23,
        )
        assertEquals(PetState.SCARED, StateResolver.resolve(inputs))
    }

    @Test
    fun `la actividad gana sobre el clima`() {
        val inputs = SensorInputs(motion = MotionType.WALK, isRaining = true, temperatureC = 35f)
        assertEquals(PetState.WALK, StateResolver.resolve(inputs))
    }

    @Test
    fun `correr gana sobre caminar por orden de candidatos y escaleras sobre ambos`() {
        assertEquals(PetState.RUN, StateResolver.resolve(SensorInputs(motion = MotionType.RUN)))
        assertEquals(PetState.CLIMB, StateResolver.resolve(SensorInputs(motion = MotionType.RUN, climbing = true)))
    }

    @Test
    fun `lluvia gana sobre noche`() {
        assertEquals(PetState.RAIN, StateResolver.resolve(SensorInputs(isRaining = true, hourOfDay = 23)))
    }

    @Test
    fun `calor y frio por umbral`() {
        assertEquals(PetState.HOT, StateResolver.resolve(SensorInputs(temperatureC = 32f)))
        assertEquals(PetState.COLD, StateResolver.resolve(SensorInputs(temperatureC = 10f)))
        assertEquals(PetState.IDLE, StateResolver.resolve(SensorInputs(temperatureC = 22f)))
    }

    @Test
    fun `pulso alto emociona y pulso bajo solo calma si esta quieto`() {
        assertEquals(PetState.EXCITED, StateResolver.resolve(SensorInputs(heartRate = 130)))
        assertEquals(PetState.CALM, StateResolver.resolve(SensorInputs(heartRate = 55)))
        // caminando con pulso bajo: la actividad gana y no hay calma
        assertEquals(PetState.WALK, StateResolver.resolve(SensorInputs(heartRate = 55, motion = MotionType.WALK)))
    }

    @Test
    fun `noche y aburrimiento`() {
        assertEquals(PetState.NIGHT, StateResolver.resolve(SensorInputs(hourOfDay = 23)))
        assertEquals(PetState.NIGHT, StateResolver.resolve(SensorInputs(hourOfDay = 3)))
        assertEquals(PetState.BORED, StateResolver.resolve(SensorInputs(hourOfDay = 12, secondsStill = 300)))
        assertEquals(PetState.NIGHT, StateResolver.resolve(SensorInputs(hourOfDay = 23, secondsStill = 600)))
    }
}
