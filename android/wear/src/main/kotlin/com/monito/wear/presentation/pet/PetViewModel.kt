package com.monito.wear.presentation.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monito.shared.protocol.DataLayerPaths
import com.monito.shared.protocol.EventMessage
import com.monito.shared.protocol.SampleMessage
import com.monito.shared.protocol.StateMessage
import com.monito.shared.state.PetState
import com.monito.wear.di.WatchContainer
import com.monito.wear.domain.model.MotionType
import com.monito.wear.domain.model.SensorInputs
import com.monito.wear.domain.rules.SensorSanity
import com.monito.wear.domain.rules.StateResolver
import com.monito.wear.domain.rules.Thresholds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Une sensores + datos del móvil, decide el estado (StateResolver) y reporta al móvil
 * cada cambio de estado, caricia, sacudida y una muestra por minuto.
 */
class PetViewModel(private val container: WatchContainer) : ViewModel() {

    private val thresholds = Thresholds()
    private val inputs = MutableStateFlow(SensorInputs())
    private val forcedState = MutableStateFlow<PetState?>(null)
    private val ignoreHeartRate = MutableStateFlow(false)

    private var shakeJob: Job? = null
    private var petJob: Job? = null

    val uiState: StateFlow<PetUiState> = combine(inputs, forcedState, ignoreHeartRate) { current, forced, hrIgnored ->
        PetUiState(
            state = forced ?: StateResolver.resolve(current, thresholds),
            forced = forced != null,
            inputs = current,
            heartRateIgnored = hrIgnored,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PetUiState())

    init {
        container.sensorHub.start()
        collectSources()
        startTicker()
        reportStateChanges()
        startSampleReporting()
    }

    private fun collectSources() {
        val hub = container.sensorHub
        viewModelScope.launch { hub.temperature.collect { t -> inputs.update { it.copy(temperatureC = SensorSanity.temperature(t)) } } }
        viewModelScope.launch { hub.motion.collect { m -> inputs.update { it.copy(motion = m) } } }
        viewModelScope.launch {
            combine(container.heartRate.bpm, ignoreHeartRate) { bpm, ignored ->
                if (ignored) null else SensorSanity.heartRate(bpm)
            }.collect { hr -> inputs.update { it.copy(heartRate = hr) } }
        }
        viewModelScope.launch { container.phoneData.isRaining.collect { r -> inputs.update { it.copy(isRaining = r) } } }
        viewModelScope.launch { hub.shakes.collect { onShake() } }
    }

    /** Cada segundo: escaleras y tiempo quieto (los que no dependen de un evento). */
    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val now = System.currentTimeMillis()
                val climbing = container.sensorHub.isClimbing(now)
                inputs.update {
                    val still = it.motion == MotionType.STILL && !climbing
                    it.copy(
                        climbing = climbing,
                        secondsStill = if (still) it.secondsStill + 1 else 0,
                    )
                }
            }
        }
    }

    private fun reportStateChanges() {
        viewModelScope.launch {
            uiState.map { it.state }.distinctUntilChanged().collect { state ->
                container.phoneMessenger.send(
                    DataLayerPaths.STATE,
                    StateMessage(state, System.currentTimeMillis()).toBytes(),
                )
            }
        }
    }

    private fun startSampleReporting() {
        viewModelScope.launch {
            while (true) {
                delay(SAMPLE_INTERVAL_MS)
                val hub = container.sensorHub
                container.phoneMessenger.send(
                    DataLayerPaths.SAMPLE,
                    SampleMessage(
                        recordedAt = System.currentTimeMillis(),
                        temperatureC = hub.temperature.value,
                        heartRate = container.heartRate.bpm.value,
                        steps = hub.steps.value,
                        pressureHpa = hub.pressure.value,
                    ).toBytes(),
                )
            }
        }
    }

    /** Toque en pantalla = caricia. */
    fun onPet() {
        petJob?.cancel()
        petJob = viewModelScope.launch {
            inputs.update { it.copy(petActive = true, secondsStill = 0) }
            container.phoneMessenger.send(DataLayerPaths.EVENT_PET, EventMessage(System.currentTimeMillis()).toBytes())
            delay(PET_DURATION_MS)
            inputs.update { it.copy(petActive = false) }
        }
    }

    private fun onShake() {
        shakeJob?.cancel()
        shakeJob = viewModelScope.launch {
            inputs.update { it.copy(shakeActive = true, secondsStill = 0) }
            container.phoneMessenger.send(DataLayerPaths.EVENT_SHAKE, EventMessage(System.currentTimeMillis()).toBytes())
            delay(SHAKE_DURATION_MS)
            inputs.update { it.copy(shakeActive = false) }
        }
    }

    /** Modo dormir: lo activa o desactiva el usuario. Al despertar no arranca ya aburrido. */
    fun setSleeping(on: Boolean) {
        forcedState.value = null
        inputs.update { it.copy(sleepMode = on, secondsStill = 0) }
    }

    fun toggleSleep() = setSleeping(!inputs.value.sleepMode)

    /** Ignora el pulso (para probar sin que el emulador dispare emocionado/tranquilo). */
    fun toggleIgnoreHeartRate() {
        ignoreHeartRate.value = !ignoreHeartRate.value
    }

    /** Panel de simulación: null vuelve al modo automático. */
    fun force(state: PetState?) {
        forcedState.value = state
    }

    override fun onCleared() {
        container.sensorHub.stop()
        container.heartRate.stop()
    }

    private companion object {
        const val SAMPLE_INTERVAL_MS = 60_000L
        const val PET_DURATION_MS = 2_000L
        const val SHAKE_DURATION_MS = 4_000L
    }
}
