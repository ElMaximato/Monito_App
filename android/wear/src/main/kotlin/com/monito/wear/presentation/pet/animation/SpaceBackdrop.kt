package com.monito.wear.presentation.pet.animation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** 10 cuadros por segundo: el fondo se mueve muy despacio, no necesita más (y cuida la batería). */
private const val BACKDROP_FRAME_MS = 100L

/**
 * Fondo del reloj (ver SpaceScene). Ocupa toda la pantalla y se alinea con el escenario del monito
 * usando el mismo margen (stagePadding) que PetStage, para que los pies queden sobre la Luna.
 */
@Composable
fun SpaceBackdrop(modifier: Modifier = Modifier, stagePadding: Dp = 8.dp) {
    var seconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = System.nanoTime()
        while (true) {
            seconds = (System.nanoTime() - start) / 1_000_000_000f
            delay(BACKDROP_FRAME_MS)
        }
    }

    Canvas(modifier) {
        drawRect(Color(SpaceScene.SKY))
        val pad = stagePadding.toPx()
        val availW = size.width - 2f * pad
        val availH = size.height - 2f * pad
        val scale = min(availW / STAGE_W, availH / STAGE_H)
        val origin = Offset(pad + (availW - STAGE_W * scale) / 2f, pad + (availH - STAGE_H * scale) / 2f)
        val boxes = SpaceScene.boxes(
            t = seconds,
            xMin = -origin.x / scale, xMax = (size.width - origin.x) / scale,
            yMin = -origin.y / scale, yMax = (size.height - origin.y) / scale,
        )
        for (b in boxes) {
            val l = (origin.x + b.x * scale).roundToInt()
            val t = (origin.y + b.y * scale).roundToInt()
            val r = (origin.x + (b.x + b.w) * scale).roundToInt()
            val bt = (origin.y + (b.y + b.h) * scale).roundToInt()
            drawRect(
                color = Color(b.color),
                topLeft = Offset(l.toFloat(), t.toFloat()),
                size = Size(max(r - l, 1).toFloat(), max(bt - t, 1).toFloat()),
            )
        }
    }
}
