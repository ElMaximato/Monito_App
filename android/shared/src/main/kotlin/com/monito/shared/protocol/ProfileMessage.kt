package com.monito.shared.protocol

import org.json.JSONObject

/** Datos del perfil que el reloj puede mostrar. */
data class ProfileMessage(val name: String, val level: Int, val personality: String) {
    fun toBytes(): ByteArray =
        JSONObject().put("name", name).put("level", level).put("personality", personality).toString().toByteArray()

    companion object {
        fun fromBytes(bytes: ByteArray): ProfileMessage {
            val json = JSONObject(String(bytes))
            return ProfileMessage(json.getString("name"), json.getInt("level"), json.getString("personality"))
        }
    }
}
