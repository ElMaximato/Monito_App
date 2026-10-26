package com.monito.mobile.watch

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.monito.mobile.MonitoApplication

/** Android despierta este servicio cuando llega un mensaje del reloj. */
class WatchListenerService : WearableListenerService() {

    override fun onMessageReceived(event: MessageEvent) {
        val container = (application as MonitoApplication).container
        container.watchEventHandler.handle(event.path, event.data)
    }
}
