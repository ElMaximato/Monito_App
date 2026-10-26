package com.monito.wear.presentation.debug

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Text
import com.monito.shared.state.PetState

/**
 * Menú oculto (pulsación larga) para forzar cualquier estado sin depender de los sensores.
 * Sirve para la demo en el emulador.
 */
@Composable
fun DebugPanel(
    current: PetState,
    forced: Boolean,
    onForce: (PetState?) -> Unit,
    onClose: () -> Unit,
) {
    ScalingLazyColumn(modifier = Modifier.fillMaxWidth()) {
        item { Text("Simulación", modifier = Modifier.padding(top = 16.dp)) }
        item {
            Chip(
                onClick = { onForce(null); onClose() },
                label = { Text(if (forced) "Volver a automático" else "Automático (activo)") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        PetState.entries.forEach { state ->
            item {
                Chip(
                    onClick = { onForce(state); onClose() },
                    label = { Text(state.displayName + if (forced && state == current) " ✓" else "") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item {
            Chip(
                onClick = onClose,
                label = { Text("Cerrar") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
