package com.monito.mobile.presentation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.monito.mobile.di.AppContainer
import com.monito.mobile.presentation.hub.HubViewModel
import com.monito.mobile.presentation.profile.ProfileViewModel

fun hubViewModelFactory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        HubViewModel(container.ensurePet, container.watchMessenger, container.watchState)
    }
}

fun profileViewModelFactory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        ProfileViewModel(
            container.getPet,
            container.getStateMinutes,
            container.updatePet,
            container.watchMessenger,
        )
    }
}
