package com.monito.wear.presentation.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Text
import com.monito.wear.presentation.debug.DebugPanel
import com.monito.wear.presentation.pet.animation.PetStage

/**
 * Pantalla principal del reloj.
 * Toque = caricia · Pulsación larga = panel de simulación (para la demo).
 */
@Composable
fun PetScreen(viewModel: PetViewModel) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    var showDebug by remember { mutableStateOf(false) }

    if (showDebug) {
        DebugPanel(
            current = ui.state,
            forced = ui.forced,
            onForce = viewModel::force,
            onClose = { showDebug = false },
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.onPet() },
                    onLongPress = { showDebug = true },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        PetStage(state = ui.state, modifier = Modifier.fillMaxSize().padding(8.dp))

        Text(
            text = ui.state.displayName + if (ui.forced) " (sim)" else "",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
        )
    }
}
