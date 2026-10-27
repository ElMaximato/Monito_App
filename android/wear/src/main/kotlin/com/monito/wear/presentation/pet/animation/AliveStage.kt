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
import com.monito.shared.state.PetState
import com.monito.wear.R
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.roundToInt

/** 15 cuadros por segundo: suficiente para esta animación y cuida la batería. */
private const val FRAME_MS = 66L

/**
 * Dibuja la escena de un estado (ver PetScenes): cuerpo fijo + ojos, accesorios y partículas en código.
 * El reloj de la escena arranca en 0 cada vez que cambia el estado, así la intro (sacar el paraguas,
 * ponerse la bufanda...) ocurre una sola vez y después la escena se queda en su pose final.
 */
@Composable
fun AliveStage(state: PetState, modifier: Modifier = Modifier) {
    val standing = ImageBitmap.imageResource(R.drawable.astro_base)
    val sitting = ImageBitmap.imageResource(R.drawable.astro_sit)
    var seconds by remember(state) { mutableFloatStateOf(0f) }

    LaunchedEffect(state) {
        val start = System.nanoTime()
        while (true) {
            seconds = (System.nanoTime() - start) / 1_000_000_000f
            delay(FRAME_MS)
        }
    }

    Canvas(modifier) {
        val frame = PetScenes.frame(state, seconds) ?: return@Canvas
        val (scale, origin) = stageTransform()
        drawBoxes(frame.behind, scale, origin)
        drawBody(if (frame.body == Body.SITTING) sitting else standing, frame, scale, origin)
        drawBoxes(frame.erase, scale, origin)
        drawBoxes(frame.face, scale, origin)
        drawBoxes(frame.front, scale, origin)
    }
}

private fun DrawScope.drawBody(sprite: ImageBitmap, f: SceneFrame, scale: Float, origin: Offset) {
    val left = (origin.x + f.dx * scale).roundToInt()
    val width = (STAGE_W * scale).roundToInt()
    val w = STAGE_W.toInt()

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

    // Respirando: lo de abajo (piernas, o la roca si está sentado) se queda quieto y casco + torso se mueven
    val cut = f.body.legsY
    val lowerHeight = STAGE_H.toInt() - cut
    drawImage(
        image = sprite,
        srcOffset = IntOffset(0, cut),
        srcSize = IntSize(w, lowerHeight),
        dstOffset = IntOffset(left, (origin.y + (cut + f.hopY) * scale).roundToInt()),
        dstSize = IntSize(width, (lowerHeight * scale).roundToInt()),
        filterQuality = FilterQuality.None,
    )
    drawImage(
        image = sprite,
        srcOffset = IntOffset(0, 0),
        srcSize = IntSize(w, cut),
        dstOffset = IntOffset(left, (origin.y + (f.bobY + f.hopY) * scale).roundToInt()),
        dstSize = IntSize(width, (cut * scale).roundToInt()),
        filterQuality = FilterQuality.None,
    )
}

/** Cajas en coordenadas del escenario, con bordes redondeados a píxel de pantalla. */
private fun DrawScope.drawBoxes(boxes: List<Box>, scale: Float, origin: Offset) {
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
