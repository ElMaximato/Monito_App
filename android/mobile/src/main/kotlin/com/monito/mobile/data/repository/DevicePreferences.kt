package com.monito.mobile.data.repository

import android.content.Context
import java.util.UUID

/** Identificador anónimo de este teléfono (sustituye al login en el MVP). */
class DevicePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("monito_prefs", Context.MODE_PRIVATE)

    val deviceId: String
        get() = prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }

    private companion object {
        const val KEY_DEVICE_ID = "device_id"
    }
}
