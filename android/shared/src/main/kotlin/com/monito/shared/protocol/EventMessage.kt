package com.monito.shared.protocol

import org.json.JSONObject

/** Interacción puntual (sacudida o caricia). */
data class EventMessage(val at: Long) {
    fun toBytes(): ByteArray = JSONObject().put("at", at).toString().toByteArray()

    companion object {
        fun fromBytes(bytes: ByteArray) = EventMessage(JSONObject(String(bytes)).getLong("at"))
    }
}
