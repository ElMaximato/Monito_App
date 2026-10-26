package com.monito.wear.presentation.pet

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.monito.wear.di.WatchContainer

fun petViewModelFactory(container: WatchContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { PetViewModel(container) }
}
