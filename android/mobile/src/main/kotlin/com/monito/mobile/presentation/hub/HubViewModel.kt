package com.monito.mobile.presentation.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monito.mobile.domain.model.Pet
import com.monito.mobile.domain.usecase.EnsurePetUseCase
import com.monito.mobile.watch.WatchMessenger
import com.monito.mobile.watch.WatchStateHolder
import com.monito.shared.protocol.DataLayerPaths
import com.monito.shared.protocol.ProfileMessage
import com.monito.shared.state.PetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HubUiState(
    val loading: Boolean = true,
    val pet: Pet? = null,
    val error: String? = null,
)

class HubViewModel(
    private val ensurePet: EnsurePetUseCase,
    private val messenger: WatchMessenger,
    watchState: WatchStateHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HubUiState())
    val uiState: StateFlow<HubUiState> = _uiState.asStateFlow()

    /** Último estado que reportó el reloj. */
    val watchState: StateFlow<PetState?> = watchState.state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = HubUiState(loading = true)
            runCatching { ensurePet() }
                .onSuccess { pet ->
                    _uiState.value = HubUiState(loading = false, pet = pet)
                    messenger.send(
                        DataLayerPaths.PROFILE,
                        ProfileMessage(pet.name, pet.level, pet.personality).toBytes(),
                    )
                }
                .onFailure {
                    _uiState.value = HubUiState(
                        loading = false,
                        error = "No se pudo conectar con el backend. ¿Está encendido?",
                    )
                }
        }
    }
}
