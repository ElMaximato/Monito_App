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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.monito.wear.R
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.roundToInt

private val EyeColor = Color(0xFF40E8DA)   // mismos colores que el hub del móvil
private val EyeShine = Color(0xFFB4FFF6)

// Ojos sobre astro_base.png (el visor viene vacío). Coordenadas del escenario 390 x 629.
private const val EYE_LEFT_X = 142f
private const val EYE_RIGHT_X = 232f
private const val EYE_Y = 244f
private const val EYE_W = 16f
private const val EYE_H = 38f

/** Desde esta fila hacia abajo están las piernas: no se mueven cuando respira. */
private const val LEGS_Y = 487

/** 15 cuadros por segundo: suficiente para parpadeos y cuida la batería. */
private const val FRAME_MS = 66L

/** Idle con vida: cuerpo fijo + ojos, respiración y saltitos dibujados en código. */
@Composable
fun AliveIdle(modifier: Modifier = Modifier) {
    val sprite = ImageBitmap.imageResource(R.drawable.astro_base)
    var seconds by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val start = System.nanoTime()
        while (true) {
            seconds = (System.nanoTime() - start) / 1_000_000_000f
            delay(FRAME_MS)
        }
    }

    Canvas(modifier) {
        val frame = IdleMotion.frame(seconds)
        val (scale, origin) = stageTransform()
        drawBody(sprite, frame, scale, origin)
        drawEyes(frame, scale, origin)
    }
}

private fun DrawScope.drawBody(sprite: ImageBitmap, f: IdleFrame, scale: Float, origin: Offset) {
    val left = origin.x.roundToInt()
    val width = (STAGE_W * scale).roundToInt()

    if (f.bobY == 0f) {
        // Entero (también cuando salta): sin costuras
        drawImage(
            image = sprite,
            dstOffset = IntOffset(left, (origin.y + f.hopY * scale).roundToInt()),
            dstSize = IntSize(width, (STAGE_H * scale).roundToInt()),
            filterQuality = FilterQuality.None,
        )
        return
    }

    // Respirando: piernas fijas y, encima, casco + torso desplazados
    val legsHeight = STAGE_H.toInt() - LEGS_Y
    drawImage(
        image = sprite,
        srcOffset = IntOffset(0, LEGS_Y),
        srcSize = IntSize(STAGE_W.toInt(), legsHeight),
        dstOffset = IntOffset(left, (origin.y + LEGS_Y * scale).roundToInt()),
        dstSize = IntSize(width, (legsHeight * scale).roundToInt()),
        filterQuality = FilterQuality.None,
    )
    drawImage(
        image = sprite,
        srcOffset = IntOffset(0, 0),
        srcSize = IntSize(STAGE_W.toInt(), LEGS_Y),
        dstOffset = IntOffset(left, (origin.y + f.bobY * scale).roundToInt()),
        dstSize = IntSize(width, (LEGS_Y * scale).roundToInt()),
        filterQuality = FilterQuality.None,
    )
}

private fun DrawScope.drawEyes(f: IdleFrame, scale: Float, origin: Offset) {
    val dy = f.bobY + f.hopY
    for (x in floatArrayOf(EYE_LEFT_X, EYE_RIGHT_X)) {
        val ex = x + f.glanceX
        val ey = EYE_Y + f.glanceY + dy
        when (f.eyes) {
            EyeStyle.OPEN -> {
                stageRect(origin, scale, ex, ey, EYE_W, EYE_H, EyeColor)
                stageRect(origin, scale, ex + 3f, ey + 4f, 6f, 6f, EyeShine)
            }
            EyeStyle.CLOSED ->
                stageRect(origin, scale, ex, ey + EYE_H / 2f - 4f, EYE_W, 8f, EyeColor)
            EyeStyle.HAPPY -> {
                val cx = ex + EYE_W / 2f
                val cy = ey + EYE_H / 2f
                stageRect(origin, scale, cx - 6f, cy - 8f, 12f, 8f, EyeColor)
                stageRect(origin, scale, cx - 14f, cy, 10f, 8f, EyeColor)
                stageRect(origin, scale, cx + 4f, cy, 10f, 8f, EyeColor)
            }
        }
    }
}

/** Rectángulo en coordenadas del escenario, con bordes redondeados a píxel de pantalla. */
private fun DrawScope.stageRect(
    origin: Offset, scale: Float, x: Float, y: Float, w: Float, h: Float, color: Color,
) {
    val l = (origin.x + x * scale).roundToInt()
    val t = (origin.y + y * scale).roundToInt()
    val r = (origin.x + (x + w) * scale).roundToInt()
    val b = (origin.y + (y + h) * scale).roundToInt()
    drawRect(color, Offset(l.toFloat(), t.toFloat()), Size(max(r - l, 1).toFloat(), max(b - t, 1).toFloat()))
}
