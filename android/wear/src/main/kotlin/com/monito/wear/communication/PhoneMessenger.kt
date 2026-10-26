package com.monito.wear.communication

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/**
 * Envía mensajes al móvil. Si el móvil no está conectado, los guarda en una cola
 * (en memoria, máx. [MAX_QUEUE]) y los reenvía en el siguiente envío exitoso.
 * Pendiente: persistir la cola en disco.
 */
class PhoneMessenger(context: Context) {
    private val nodeClient = Wearable.getNodeClient(context)
    private val messageClient = Wearable.getMessageClient(context)

    private val outbox = ArrayDeque<Pair<String, ByteArray>>()
    private val lock = Mutex()

    suspend fun send(path: String, payload: ByteArray) = lock.withLock {
        outbox.addLast(path to payload)
        while (outbox.size > MAX_QUEUE) outbox.removeFirst()
        flushLocked()
    }

    suspend fun flush() = lock.withLock { flushLocked() }

    private suspend fun flushLocked() {
        val nodes = runCatching { nodeClient.connectedNodes.await() }.getOrNull().orEmpty()
        if (nodes.isEmpty()) return

        while (outbox.isNotEmpty()) {
            val (path, payload) = outbox.first()
            val sent = runCatching {
                nodes.forEach { messageClient.sendMessage(it.id, path, payload).await() }
            }.onFailure { Log.w(TAG, "No se pudo enviar $path", it) }.isSuccess
            if (!sent) return
            outbox.removeFirst()
        }
    }

    private companion object {
        const val TAG = "PhoneMessenger"
        const val MAX_QUEUE = 100
    }
}
