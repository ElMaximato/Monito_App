package com.monito.wear.communication

import com.monito.shared.protocol.ProfileMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Último clima y perfil recibidos del móvil. Sin mensaje de clima se asume que no llueve. */
class PhoneDataStore {
    private val _isRaining = MutableStateFlow(false)
    val isRaining: StateFlow<Boolean> = _isRaining.asStateFlow()

    private val _profile = MutableStateFlow<ProfileMessage?>(null)
    val profile: StateFlow<ProfileMessage?> = _profile.asStateFlow()

    fun updateRaining(value: Boolean) {
        _isRaining.value = value
    }

    fun updateProfile(value: ProfileMessage) {
        _profile.value = value
    }
}
