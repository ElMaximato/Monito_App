package com.monito.mobile.presentation.profile

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monito.mobile.domain.model.Pet
import com.monito.mobile.domain.model.StateMinutes
import com.monito.mobile.presentation.components.XpBar
import kotlin.math.roundToInt

private val PanelColor = Color(0xB3101426)   // azul noche, 70 % opaco
private val Teal = Color(0xFF2EE6D0)
private val Personalities = listOf("curioso", "juguetón", "tímido", "valiente", "soñador", "tranquilo")

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onBack: () -> Unit) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showRename by remember { mutableStateOf(false) }
    var showPersonality by remember { mutableStateOf(false) }

    LaunchedEffect(ui.notice) {
        ui.notice?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearNotice()
        }
    }

    // aparece con un fundido suave, como en los créditos de un anime
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val appear by animateFloatAsState(if (started) 1f else 0f, tween(1100), label = "appear")

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0C18))) {
            ProfileScene(modifier = Modifier.fillMaxSize())

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val sceneHeight = maxHeight * 0.58f
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .graphicsLayer {
                            alpha = appear
                            translationY = (1f - appear) * 60f
                        },
                ) {
                    Spacer(Modifier.height(sceneHeight))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val pet = ui.pet
                        when {
                            ui.loading -> Panel { CircularProgressIndicator(color = Teal) }
                            pet == null -> Panel {
                                Text(ui.error ?: "Sin datos")
                                TextButton(onClick = viewModel::load) { Text("Reintentar", color = Teal) }
                            }
                            else -> {
                                HeaderPanel(pet, onEdit = { showRename = true })
                                StatsPanel(pet)
                                TimePanel(ui.minutesByState)
                                OptionsPanel(
                                    saving = ui.saving,
                                    onRename = { showRename = true },
                                    onPersonality = { showPersonality = true },
                                    onRefresh = viewModel::load,
                                )
                            }
                        }
                    }
                }
            }

            // barra superior fija
            Box(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Text("← Volver", color = Teal)
                }
                Text(
                    "P E R F I L",
                    modifier = Modifier.align(Alignment.Center).alpha(0.75f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }

    val pet = ui.pet
    if (showRename && pet != null) {
        RenameDialog(
            current = pet.name,
            onDismiss = { showRename = false },
            onConfirm = {
                showRename = false
                viewModel.rename(it)
            },
        )
    }
    if (showPersonality && pet != null) {
        PersonalityDialog(
            current = pet.personality,
            onDismiss = { showPersonality = false },
            onSelect = {
                showPersonality = false
                viewModel.setPersonality(it)
            },
        )
    }
}

@Composable
private fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = PanelColor,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { content() }
    }
}

@Composable
private fun HeaderPanel(pet: Pet, onEdit: () -> Unit) {
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(pet.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Nivel ${pet.level} · ${pet.personality}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Teal,
                )
            }
            TextButton(onClick = onEdit) { Text("Editar", color = Teal) }
        }
        XpBar(progress = pet.levelProgress, modifier = Modifier.padding(top = 4.dp))
        Text(
            "${pet.xp} XP · siguiente nivel en ${pet.nextLevelXp} XP",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alpha(0.8f),
        )
    }
}

@Composable
private fun StatsPanel(pet: Pet) {
    Panel {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Stat("Caricias", pet.petCount.toString())
            Stat("Sacudidas", pet.shakeCount.toString())
            Stat("XP total", pet.xp.toString())
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.alpha(0.75f))
    }
}

@Composable
private fun TimePanel(items: List<StateMinutes>) {
    Panel {
        Text("Tiempo por estado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (items.isEmpty()) {
            Text("Todavía no hay historial. Mueve el reloj un rato.", modifier = Modifier.alpha(0.8f))
        } else {
            val sorted = items.sortedByDescending { it.minutes }.take(6)
            val top = sorted.first().minutes.coerceAtLeast(0.01)
            sorted.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.state.displayName, style = MaterialTheme.typography.bodyMedium)
                        Text("${item.minutes.roundToInt()} min", style = MaterialTheme.typography.bodyMedium)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(3.dp)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((item.minutes / top).toFloat().coerceIn(0.02f, 1f))
                                .height(6.dp)
                                .background(Teal, RoundedCornerShape(3.dp)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionsPanel(
    saving: Boolean,
    onRename: () -> Unit,
    onPersonality: () -> Unit,
    onRefresh: () -> Unit,
) {
    Panel {
        Text("Opciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        OutlinedButton(onClick = onRename, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
            Text("Cambiar nombre", color = Color.White)
        }
        OutlinedButton(onClick = onPersonality, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
            Text("Cambiar personalidad", color = Color.White)
        }
        OutlinedButton(onClick = onRefresh, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
            Text("Actualizar datos", color = Color.White)
        }
        Text(
            if (saving) "Guardando…" else "Los cambios también se envían al reloj.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alpha(0.7f),
        )
    }
}

@Composable
private fun RenameDialog(current: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var draft by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nombre del monito") },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(24) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft) }, enabled = draft.trim().isNotEmpty()) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun PersonalityDialog(current: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personalidad") },
        text = {
            Column {
                Personalities.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == current, onClick = { onSelect(option) })
                        TextButton(onClick = { onSelect(option) }) { Text(option) }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
    )
}
