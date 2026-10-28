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
    fun `la caricia gana sobre todo lo demas`() {
        val inputs = SensorInputs(
            petActive = true, shakeActive = true, sleepMode = true, isRaining = true,
            motion = MotionType.RUN, temperatureC = 40f, heartRate = 130,
        )
        assertEquals(PetState.PET, StateResolver.resolve(inputs))
    }

    @Test
    fun `la sacudida gana sobre actividad y clima`() {
        val inputs = SensorInputs(
            shakeActive = true, isRaining = true,
            motion = MotionType.RUN, temperatureC = 40f,
        )
        assertEquals(PetState.SCARED, StateResolver.resolve(inputs))
    }

    @Test
    fun `la actividad gana sobre el clima`() {
        val inputs = SensorInputs(motion = MotionType.WALK, isRaining = true, temperatureC = 35f)
        assertEquals(PetState.WALK, StateResolver.resolve(inputs))
    }

    @Test
    fun `correr y subir escaleras se ven como caminar`() {
        assertEquals(PetState.WALK, StateResolver.resolve(SensorInputs(motion = MotionType.RUN)))
        assertEquals(PetState.WALK, StateResolver.resolve(SensorInputs(motion = MotionType.RUN, climbing = true)))
        assertEquals(PetState.WALK, StateResolver.resolve(SensorInputs(climbing = true)))
    }

    @Test
    fun `lluvia gana sobre aburrido`() {
        assertEquals(PetState.RAIN, StateResolver.resolve(SensorInputs(isRaining = true, secondsStill = 600)))
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
    fun `aburrimiento por tiempo quieto`() {
        assertEquals(PetState.IDLE, StateResolver.resolve(SensorInputs(secondsStill = 299)))
        assertEquals(PetState.BORED, StateResolver.resolve(SensorInputs(secondsStill = 300)))
    }

    @Test
    fun `dormir es un modo y no depende de la hora`() {
        // sin el modo activo, ninguna hora hace dormir (ya no existe hourOfDay)
        assertEquals(PetState.IDLE, StateResolver.resolve(SensorInputs()))
        assertEquals(PetState.NIGHT, StateResolver.resolve(SensorInputs(sleepMode = true)))
    }

    @Test
    fun `dormir gana sobre sensores pero no sobre la caricia`() {
        val everything = SensorInputs(
            sleepMode = true, shakeActive = true, isRaining = true, temperatureC = 40f,
            heartRate = 130, motion = MotionType.WALK, secondsStill = 600,
        )
        assertEquals(PetState.NIGHT, StateResolver.resolve(everything))
        assertEquals(PetState.PET, StateResolver.resolve(everything.copy(petActive = true)))
        // al terminar la caricia vuelve a dormir
        assertEquals(PetState.NIGHT, StateResolver.resolve(everything.copy(petActive = false)))
    }
}
