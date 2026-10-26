package com.monito.mobile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monito.mobile.domain.model.Pet
import com.monito.mobile.domain.model.StateMinutes
import com.monito.mobile.domain.usecase.GetPetUseCase
import com.monito.mobile.domain.usecase.GetStateMinutesUseCase
import com.monito.mobile.domain.usecase.UpdatePetUseCase
import com.monito.mobile.watch.WatchMessenger
import com.monito.shared.protocol.DataLayerPaths
import com.monito.shared.protocol.ProfileMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = true,
    val pet: Pet? = null,
    val minutesByState: List<StateMinutes> = emptyList(),
    val error: String? = null,
    val saving: Boolean = false,
    /** Mensaje corto para mostrar una sola vez (guardado / error). */
    val notice: String? = null,
)

class ProfileViewModel(
    private val getPet: GetPetUseCase,
    private val getStateMinutes: GetStateMinutesUseCase,
    private val updatePet: UpdatePetUseCase,
    private val messenger: WatchMessenger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = it.pet == null, error = null) }
            runCatching { getPet() to getStateMinutes() }
                .onSuccess { (pet, minutes) ->
                    _uiState.update { it.copy(loading = false, pet = pet, minutesByState = minutes, error = null) }
                }
                .onFailure {
                    _uiState.update { it.copy(loading = false, error = "No se pudo cargar el perfil.") }
                }
        }
    }

    fun rename(name: String) {
        val clean = name.trim()
        if (clean.isNotEmpty() && clean != _uiState.value.pet?.name) save(name = clean, personality = null)
    }

    fun setPersonality(personality: String) {
        if (personality != _uiState.value.pet?.personality) save(name = null, personality = personality)
    }

    fun clearNotice() {
        _uiState.update { it.copy(notice = null) }
    }

    private fun save(name: String?, personality: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true) }
            runCatching { updatePet(name, personality) }
                .onSuccess { pet ->
                    _uiState.update { it.copy(pet = pet, saving = false, notice = "Guardado") }
                    // el reloj también se entera del cambio
                    messenger.send(
                        DataLayerPaths.PROFILE,
                        ProfileMessage(pet.name, pet.level, pet.personality).toBytes(),
                    )
                }
                .onFailure {
                    _uiState.update { it.copy(saving = false, notice = "No se pudo guardar") }
                }
        }
    }
}
