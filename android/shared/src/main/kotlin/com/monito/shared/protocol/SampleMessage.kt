package com.monito.shared.protocol

import org.json.JSONObject

/** Muestra periódica de sensores. Los campos pueden faltar si el sensor no existe. */
data class SampleMessage(
    val recordedAt: Long,
    val temperatureC: Float? = null,
    val heartRate: Int? = null,
    val steps: Int? = null,
    val pressureHpa: Float? = null,
) {
    fun toBytes(): ByteArray {
        val json = JSONObject().put("recordedAt", recordedAt)
        temperatureC?.let { json.put("temperatureC", it.toDouble()) }
        heartRate?.let { json.put("heartRate", it) }
        steps?.let { json.put("steps", it) }
        pressureHpa?.let { json.put("pressureHpa", it.toDouble()) }
        return json.toString().toByteArray()
    }

    companion object {
        fun fromBytes(bytes: ByteArray): SampleMessage {
            val json = JSONObject(String(bytes))
            return SampleMessage(
                recordedAt = json.getLong("recordedAt"),
                temperatureC = if (json.has("temperatureC")) json.getDouble("temperatureC").toFloat() else null,
                heartRate = if (json.has("heartRate")) json.getInt("heartRate") else null,
                steps = if (json.has("steps")) json.getInt("steps") else null,
                pressureHpa = if (json.has("pressureHpa")) json.getDouble("pressureHpa").toFloat() else null,
            )
        }
    }
}
