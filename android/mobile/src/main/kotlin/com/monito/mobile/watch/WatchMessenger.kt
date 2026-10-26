package com.monito.mobile.watch

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/** Envía mensajes del móvil al reloj por la Wearable Data Layer. */
class WatchMessenger(context: Context) {
    private val nodeClient = Wearable.getNodeClient(context)
    private val messageClient = Wearable.getMessageClient(context)

    /** Manda el payload a todos los relojes conectados. Si no hay ninguno, no pasa nada. */
    suspend fun send(path: String, payload: ByteArray) {
        runCatching {
            nodeClient.connectedNodes.await().forEach { node ->
                messageClient.sendMessage(node.id, path, payload).await()
            }
        }.onFailure { Log.w(TAG, "No se pudo enviar $path al reloj", it) }
    }

    private companion object {
        const val TAG = "WatchMessenger"
    }
}
