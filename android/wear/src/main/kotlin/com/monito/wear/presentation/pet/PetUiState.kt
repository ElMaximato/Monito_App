package com.monito.wear.presentation.pet

import com.monito.shared.state.PetState

/** forced = true cuando el estado viene del panel de simulación. */
data class PetUiState(
    val state: PetState = PetState.IDLE,
    val forced: Boolean = false,
)
