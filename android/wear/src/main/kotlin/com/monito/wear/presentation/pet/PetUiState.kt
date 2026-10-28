package com.monito.wear.presentation.pet

import com.monito.shared.state.PetState
import com.monito.wear.domain.model.SensorInputs

/** forced = true cuando el estado viene del panel de simulación. inputs = lo que "siente" ahora (para calibrar). */
data class PetUiState(
    val state: PetState = PetState.IDLE,
    val forced: Boolean = false,
    val inputs: SensorInputs = SensorInputs(),
    /** true = el pulso se ignora (útil en el emulador, que manda un pulso sintético que sube solo). */
    val heartRateIgnored: Boolean = false,
)
