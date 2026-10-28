package com.monito.mobile.presentation.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Hub del monito: la escena animada y los botones a "Mi día" (resumen y clima) y al perfil.
 * (El ViewModel sigue creándose para que el monito se registre y el reloj reciba su perfil.)
 */
@Composable
fun HubScreen(viewModel: HubViewModel, onOpenProfile: () -> Unit, onOpenDay: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        HubScene(modifier = Modifier.fillMaxSize())

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            Button(onClick = onOpenDay) { Text("Mi día") }
            Button(onClick = onOpenProfile) { Text("Ver perfil") }
        }
    }
}
