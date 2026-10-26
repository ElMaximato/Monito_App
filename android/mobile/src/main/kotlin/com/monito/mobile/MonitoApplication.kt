package com.monito.mobile

import android.app.Application
import com.monito.mobile.di.AppContainer
import kotlinx.coroutines.launch

class MonitoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Registra el monito en el backend (si falla, el hub ofrece reintentar).
        container.appScope.launch { runCatching { container.ensurePet() } }
        container.weatherSync.start(container.appScope)
    }
}
