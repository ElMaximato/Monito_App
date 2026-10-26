package com.monito.wear.health

import android.content.Context
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/** Ritmo cardíaco vía Health Services. Requiere el permiso BODY_SENSORS. */
class HeartRateSource(context: Context) {

    private val measureClient = HealthServices.getClient(context).measureClient

    private val _bpm = MutableStateFlow<Int?>(null)
    val bpm: StateFlow<Int?> = _bpm.asStateFlow()

    private val callback = object : MeasureCallback {
        override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) = Unit

        override fun onDataReceived(data: DataPointContainer) {
            data.getData(DataType.HEART_RATE_BPM).lastOrNull()?.let { _bpm.value = it.value.roundToInt() }
        }
    }

    fun start() {
        runCatching { measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback) }
            .onFailure { Log.w(TAG, "No se pudo iniciar el ritmo cardíaco", it) }
    }

    fun stop() {
        runCatching { measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback) }
    }

    private companion object {
        const val TAG = "HeartRateSource"
    }
}
