package com.monito.shared.protocol

import org.json.JSONObject

/** Lo único que el reloj necesita del clima: ¿llueve? */
data class WeatherMessage(val isRaining: Boolean, val checkedAt: Long) {
    fun toBytes(): ByteArray =
        JSONObject().put("isRaining", isRaining).put("checkedAt", checkedAt).toString().toByteArray()

    companion object {
        fun fromBytes(bytes: ByteArray): WeatherMessage {
            val json = JSONObject(String(bytes))
            return WeatherMessage(json.getBoolean("isRaining"), json.getLong("checkedAt"))
        }
    }
}
