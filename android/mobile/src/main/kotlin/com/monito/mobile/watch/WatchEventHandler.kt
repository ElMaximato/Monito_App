package com.monito.mobile.watch

import android.util.Log
import com.monito.mobile.domain.repository.PetRepository
import com.monito.shared.protocol.DataLayerPaths
import com.monito.shared.protocol.EventMessage
import com.monito.shared.protocol.SampleMessage
import com.monito.shared.protocol.StateMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Traduce los mensajes del reloj en acciones sobre el backend. */
class WatchEventHandler(
    private val repository: PetRepository,
    private val watchState: WatchStateHolder,
    private val scope: CoroutineScope,
) {
    fun handle(path: String, data: ByteArray) {
        scope.launch {
            runCatching {
                when (path) {
                    DataLayerPaths.EVENT_PET -> repository.registerEvent("pet", EventMessage.fromBytes(data).at)
                    DataLayerPaths.EVENT_SHAKE -> repository.registerEvent("shake", EventMessage.fromBytes(data).at)
                    DataLayerPaths.SAMPLE -> repository.sendSample(SampleMessage.fromBytes(data))
                    DataLayerPaths.STATE -> {
                        val message = StateMessage.fromBytes(data)
                        watchState.update(message.state) // se muestra aunque el backend esté caído
                        repository.logState(message)
                    }
                    else -> Log.d(TAG, "Ruta ignorada: $path")
                }
            }.onFailure { Log.w(TAG, "Falló el manejo de $path", it) }
        }
    }

    private companion object {
        const val TAG = "WatchEventHandler"
    }
}
