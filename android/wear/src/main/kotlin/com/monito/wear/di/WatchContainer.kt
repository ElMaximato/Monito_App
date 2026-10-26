package com.monito.wear.di

import android.content.Context
import com.monito.wear.communication.PhoneDataStore
import com.monito.wear.communication.PhoneMessenger
import com.monito.wear.health.HeartRateSource
import com.monito.wear.sensors.SensorHub

/** Inyección de dependencias manual del reloj. */
class WatchContainer(context: Context) {
    private val appContext = context.applicationContext

    val sensorHub = SensorHub(appContext)
    val heartRate = HeartRateSource(appContext)
    val phoneMessenger = PhoneMessenger(appContext)
    val phoneData = PhoneDataStore()
}
