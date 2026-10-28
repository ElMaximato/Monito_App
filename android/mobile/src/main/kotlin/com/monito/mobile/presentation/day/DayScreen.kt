package com.monito.mobile.presentation.day

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monito.mobile.domain.model.DailySummary
import com.monito.mobile.domain.model.WeatherReport
import com.monito.mobile.domain.weather.TipSeverity
import com.monito.mobile.domain.weather.WeatherTip
import com.monito.mobile.presentation.theme.Orange
import com.monito.mobile.presentation.theme.Pink
import com.monito.mobile.presentation.theme.Teal
import kotlin.math.roundToInt

private val PanelColor = Color(0xB3101426)
private val AlertColor = Color(0xFFFF6B6B)

@Composable
fun DayScreen(viewModel: DayViewModel, onBack: () -> Unit) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0C18))) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                    TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Text("← Volver", color = Teal)
                    }
                    Text(
                        "M I   D Í A",
                        modifier = Modifier.align(Alignment.Center).alpha(0.75f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (ui.loading) {
                        Panel { CircularProgressIndicator(color = Teal) }
                    } else {
                        WeatherPanel(ui.weather, ui.weatherError, ui.tips)
                        SummaryPanel(ui.summary, ui.summaryError)
                        OutlinedButton(onClick = viewModel::load, modifier = Modifier.fillMaxWidth()) {
                            Text("Actualizar", color = Teal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = PanelColor, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun WeatherPanel(weather: WeatherReport?, failed: Boolean, tips: List<WeatherTip>) {
    Panel {
        Text("Clima de hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (weather == null) {
            Text(
                if (failed) "No se pudo consultar el clima. Revisa tu conexión." else "Sin datos del clima.",
                modifier = Modifier.alpha(0.8f),
            )
            return@Panel
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("${weather.temperatureC.roundToInt()}°", fontSize = 44.sp, fontWeight = FontWeight.Bold)
            Text(
                "Sensación ${weather.apparentC.roundToInt()}° · Máx ${weather.maxC.roundToInt()}° · " +
                    "Mín ${weather.minC.roundToInt()}°\nLluvia ${weather.rainChancePct}% · Viento ${weather.windKmh.roundToInt()} km/h",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.8f).padding(bottom = 6.dp),
            )
        }
        Text("El monito te recomienda", style = MaterialTheme.typography.labelMedium, modifier = Modifier.alpha(0.75f))
        tips.forEach { TipRow(it) }
    }
}

@Composable
private fun TipRow(tip: WeatherTip) {
    val color = when (tip.severity) {
        TipSeverity.ALERT -> AlertColor
        TipSeverity.WARN -> Orange
        TipSeverity.INFO -> Teal
    }
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(color, RoundedCornerShape(2.dp)))
        Text(tip.text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SummaryPanel(summary: DailySummary?, failed: Boolean) {
    Panel {
        Text("Tu día hasta ahora", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        when {
            summary == null -> Text(
                if (failed) "No se pudo cargar el resumen. ¿Está encendido el backend?" else "Sin datos.",
                modifier = Modifier.alpha(0.8f),
            )
            !summary.hasData -> Text(
                "Todavía no hay datos de hoy. Usa el reloj un rato y vuelve a actualizar.",
                modifier = Modifier.alpha(0.8f),
            )
            else -> SummaryContent(summary)
        }
    }
}

@Composable
private fun SummaryContent(summary: DailySummary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        StatItem("Pasos", formatSteps(summary.steps))
        StatItem("En movimiento", formatDuration(summary.movingMinutes))
        StatItem("Quieto", formatDuration(summary.restingMinutes))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        StatItem("Monito dormido", formatDuration(summary.sleepMinutes))
        StatItem("Pulso promedio", summary.heartRate?.let { "${it.avg} lpm" } ?: "—")
        StatItem("Pulso mín–máx", summary.heartRate?.let { "${it.min}–${it.max}" } ?: "—")
    }

    DayBar(summary)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        LegendItem("Movimiento", Teal)
        LegendItem("Quieto", Color.White.copy(alpha = 0.35f))
        LegendItem("Dormido", Pink)
    }
    Text(
        "Movimiento = caminar, correr y escaleras que detecta el reloj. Si no lo traes puesto, ese rato no se cuenta.",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.alpha(0.6f),
    )
}

/** Barra apilada: qué parte del tiempo registrado fue movimiento, quieto o dormido. */
@Composable
private fun DayBar(summary: DailySummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color.White.copy(alpha = 0.12f)),
    ) {
        segment(summary.movingMinutes, Teal)
        segment(summary.restingMinutes, Color.White.copy(alpha = 0.35f))
        segment(summary.sleepMinutes, Pink)
    }
}

@Composable
private fun RowScope.segment(minutes: Double, color: Color) {
    if (minutes > 0.0) {
        Box(modifier = Modifier.weight(minutes.toFloat()).fillMaxHeight().background(color))
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(104.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.alpha(0.75f))
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.alpha(0.8f))
    }
}
