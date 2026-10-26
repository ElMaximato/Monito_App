package com.monito.wear.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.monito.wear.domain.model.MotionType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Lee los sensores del reloj (o los "Virtual sensors" del emulador) y los expone como flujos.
 * Si un sensor no existe, su flujo se queda en null y se deja constancia en el log.
 */
class SensorHub(context: Context) {

    private val sensorManager = context.getSystemService(SensorManager::class.java)

    private val shakeDetector = ShakeDetector()
    private val motionClassifier = MotionClassifier()
    private val climbDetector = ClimbDetector()

    private val _temperature = MutableStateFlow<Float?>(null)
    val temperature: StateFlow<Float?> = _temperature.asStateFlow()

    private val _pressure = MutableStateFlow<Float?>(null)
    val pressure: StateFlow<Float?> = _pressure.asStateFlow()

    private val _steps = MutableStateFlow<Int?>(null)
    /** Pasos acumulados desde el último reinicio del reloj. */
    val steps: StateFlow<Int?> = _steps.asStateFlow()

    private val _motion = MutableStateFlow(MotionType.STILL)
    val motion: StateFlow<MotionType> = _motion.asStateFlow()

    private val _shakes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val shakes: SharedFlow<Unit> = _shakes.asSharedFlow()

    fun isClimbing(nowMs: Long): Boolean = climbDetector.isClimbing(nowMs)

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val now = System.currentTimeMillis()
            when (event.sensor.type) {
                Sensor.TYPE_AMBIENT_TEMPERATURE -> _temperature.value = event.values[0]
                Sensor.TYPE_PRESSURE -> {
                    _pressure.value = event.values[0]
                    climbDetector.onSample(event.values[0], now)
                }
                Sensor.TYPE_STEP_COUNTER -> _steps.value = event.values[0].toInt()
                Sensor.TYPE_ACCELEROMETER -> {
                    val (x, y, z) = event.values
                    if (shakeDetector.onSample(x, y, z, now)) _shakes.tryEmit(Unit)
                    _motion.value = motionClassifier.onSample(sqrt(x * x + y * y + z * z), now)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
    }

    fun start() {
        register(Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME)
        register(Sensor.TYPE_AMBIENT_TEMPERATURE, SensorManager.SENSOR_DELAY_NORMAL)
        register(Sensor.TYPE_PRESSURE, SensorManager.SENSOR_DELAY_NORMAL)
        register(Sensor.TYPE_STEP_COUNTER, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() = sensorManager.unregisterListener(listener)

    private fun register(type: Int, delay: Int) {
        val sensor = sensorManager.getDefaultSensor(type)
        if (sensor == null) {
            Log.w(TAG, "Este dispositivo no tiene el sensor tipo $type")
            return
        }
        sensorManager.registerListener(listener, sensor, delay)
    }

    private companion object {
        const val TAG = "SensorHub"
    }
}
