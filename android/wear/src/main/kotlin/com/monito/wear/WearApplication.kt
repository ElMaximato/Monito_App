package com.monito.wear

import android.app.Application
import com.monito.wear.di.WatchContainer

class WearApplication : Application() {
    lateinit var container: WatchContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = WatchContainer(this)
    }
}
