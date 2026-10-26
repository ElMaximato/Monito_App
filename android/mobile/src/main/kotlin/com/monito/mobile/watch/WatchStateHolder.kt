package com.monito.mobile.watch

import com.monito.shared.state.PetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Último estado que reportó el reloj (null = aún no se ha recibido ninguno). */
class WatchStateHolder {
    private val _state = MutableStateFlow<PetState?>(null)
    val state: StateFlow<PetState?> = _state.asStateFlow()

    fun update(state: PetState) {
        _state.value = state
    }
}
