package com.monito.wear.domain.rules

import com.monito.shared.state.PetState
import com.monito.wear.domain.model.MotionType
import com.monito.wear.domain.model.SensorInputs

/**
 * Decide qué estado mostrar. Reúne todos los estados que aplican ahora mismo
 * y gana el de menor número de prioridad (ver PetState). Lógica pura: se prueba
 * sin emulador (src/test).
 *
 * Orden: caricia > dormir (modo del usuario) > susto > actividad > pulso > clima > aburrido > normal.
 */
object StateResolver {

    fun resolve(inputs: SensorInputs, thresholds: Thresholds = Thresholds()): PetState {
        val candidates = buildList {
            if (inputs.petActive) add(PetState.PET)
            if (inputs.sleepMode) add(PetState.NIGHT)
            if (inputs.shakeActive) add(PetState.SCARED)

            // Correr y subir escaleras ya no tienen animación propia: se ven como caminar
            if (inputs.motion != MotionType.STILL || inputs.climbing) add(PetState.WALK)

            inputs.heartRate?.let { bpm ->
                when {
                    bpm >= thresholds.highHeartRate -> add(PetState.EXCITED)
                    bpm <= thresholds.lowHeartRate && inputs.motion == MotionType.STILL -> add(PetState.CALM)
                    else -> Unit
                }
            }

            if (inputs.isRaining) add(PetState.RAIN)
            inputs.temperatureC?.let { celsius ->
                when {
                    celsius >= thresholds.hotC -> add(PetState.HOT)
                    celsius <= thresholds.coldC -> add(PetState.COLD)
                    else -> Unit
                }
            }

            if (inputs.secondsStill >= thresholds.boredAfterSeconds) add(PetState.BORED)
            add(PetState.IDLE)
        }
        // minBy devuelve el primero con la menor prioridad
        return candidates.minBy { it.priority }
    }
}
