package com.monito.shared.protocol

import com.monito.shared.state.PetState
import org.json.JSONObject

/** El reloj cambió de estado. */
data class StateMessage(val state: PetState, val startedAt: Long) {
    fun toBytes(): ByteArray =
        JSONObject().put("state", state.name).put("startedAt", startedAt).toString().toByteArray()

    companion object {
        fun fromBytes(bytes: ByteArray): StateMessage {
            val json = JSONObject(String(bytes))
            return StateMessage(PetState.fromCode(json.getString("state")), json.getLong("startedAt"))
        }
    }
}
