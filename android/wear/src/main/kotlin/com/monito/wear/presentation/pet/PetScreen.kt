package com.monito.wear.presentation.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Text
import com.monito.wear.presentation.debug.DebugPanel
import com.monito.wear.presentation.pet.animation.PetStage
import com.monito.wear.presentation.pet.animation.SpaceBackdrop

/** true = una pulsación larga abre el panel de simulación (para forzar cada estado sin esperar a los sensores). */
private const val SHOW_DEBUG_PANEL = true

/** true = muestra abajo el estado y lo que "siente" el reloj (pulso, temperatura, movimiento), para calibrar. */
private const val SHOW_CALIBRATION = true

/**
 * Pantalla principal del reloj: fondo de espacio con suelo de Luna y el monito, que cambia solo
 * según los sensores. Toque = caricia.
 */
@Composable
fun PetScreen(viewModel: PetViewModel) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    var showDebug by remember { mutableStateOf(false) }

    if (showDebug) {
        DebugPanel(
            current = ui.state,
            forced = ui.forced,
            sleeping = ui.inputs.sleepMode,
            onForce = viewModel::force,
            onToggleSleep = viewModel::toggleSleep,
            heartRateIgnored = ui.heartRateIgnored,
            onToggleHeartRate = viewModel::toggleIgnoreHeartRate,
            onClose = { showDebug = false },
        )
        return
    }

    val onLongPress: ((Offset) -> Unit)? = if (SHOW_DEBUG_PANEL) ({ _: Offset -> showDebug = true }) else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.onPet() },
                    onLongPress = onLongPress,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        SpaceBackdrop(Modifier.fillMaxSize(), stagePadding = 8.dp)
        PetStage(state = ui.state, modifier = Modifier.fillMaxSize().padding(8.dp))

        if (SHOW_CALIBRATION) {
            val i = ui.inputs
            val readings = (i.heartRate?.let { "$it bpm" } ?: "- bpm") + "  " +
                (i.temperatureC?.let { "%.0f°".format(it) } ?: "-°") + "  " +
                i.motion.name.lowercase() + "  " + i.secondsStill + "s"
            Text(
                text = ui.state.displayName + (if (ui.forced) " (sim)" else "") + "\n" + readings,
                fontSize = 9.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
                    .background(Color(0xAA11182AL), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}
