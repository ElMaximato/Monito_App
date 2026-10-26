package com.monito.wear.communication

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.monito.shared.protocol.DataLayerPaths
import com.monito.shared.protocol.ProfileMessage
import com.monito.shared.protocol.WeatherMessage
import com.monito.wear.WearApplication

/** Recibe clima y perfil que manda el móvil. */
class PhoneListenerService : WearableListenerService() {

    override fun onMessageReceived(event: MessageEvent) {
        val store = (application as WearApplication).container.phoneData
        runCatching {
            when (event.path) {
                DataLayerPaths.WEATHER -> store.updateRaining(WeatherMessage.fromBytes(event.data).isRaining)
                DataLayerPaths.PROFILE -> store.updateProfile(ProfileMessage.fromBytes(event.data))
                else -> Unit
            }
        }.onFailure { Log.w("PhoneListener", "Mensaje inválido en ${event.path}", it) }
    }
}
