package com.monito.wear.presentation.pet.animation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.monito.shared.state.PetState
import com.monito.wear.presentation.pet.spriteResFor
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.roundToInt

/** Tamaño del escenario: el de los sprites fijos (astro_*.png). */
private const val STAGE_W = 390f
private const val STAGE_H = 629f

/**
 * Dibuja al monito para un estado: la animación si existe (SpriteAnimations) o, si no,
 * su imagen fija. Ambas comparten escenario, así que el tamaño y las botas no saltan al cambiar.
 */
@Composable
fun PetStage(state: PetState, modifier: Modifier = Modifier) {
    val animation = SpriteAnimations.forState(state)
    if (animation != null) {
        AnimatedLayer(animation, modifier)
    } else {
        StaticLayer(spriteResFor(state), modifier)
    }
}

@Composable
private fun StaticLayer(sheetRes: Int, modifier: Modifier) {
    val image = ImageBitmap.imageResource(sheetRes)
    Canvas(modifier) {
        val (scale, origin) = stageTransform()
        drawImage(
            image = image,
            dstOffset = origin.toIntOffset(),
            dstSize = IntSize((STAGE_W * scale).roundToInt(), (STAGE_H * scale).roundToInt()),
            filterQuality = FilterQuality.None, // pixel art nítido
        )
    }
}

@Composable
private fun AnimatedLayer(animation: SpriteAnimation, modifier: Modifier) {
    val sheet = ImageBitmap.imageResource(animation.sheetRes)
    var frame by remember(animation) { mutableIntStateOf(0) }

    // Reloj de la animación: arranca desde el primer cuadro cada vez que cambia el estado.
    // Se revisa 2 veces por cuadro (en vez de 60 veces por segundo) para cuidar la batería.
    LaunchedEffect(animation) {
        val start = System.nanoTime()
        while (true) {
            frame = animation.frameAt(System.nanoTime() - start)
            delay(1000L / (animation.fps.toLong() * 2))
        }
    }

    Canvas(modifier) {
        val (scale, origin) = stageTransform()
        drawImage(
            image = sheet,
            srcOffset = IntOffset(animation.column(frame) * animation.frameWidth, animation.row(frame) * animation.frameHeight),
            srcSize = IntSize(animation.frameWidth, animation.frameHeight),
            dstOffset = Offset(origin.x + animation.stageX * scale, origin.y + animation.stageY * scale).toIntOffset(),
            dstSize = IntSize((animation.stageWidth * scale).roundToInt(), (animation.stageHeight * scale).roundToInt()),
            filterQuality = FilterQuality.None,
        )
    }
}

/** Escala uniforme y esquina del escenario para que quepa centrado en el área disponible. */
private fun DrawScope.stageTransform(): Pair<Float, Offset> {
    val scale = min(size.width / STAGE_W, size.height / STAGE_H)
    val origin = Offset((size.width - STAGE_W * scale) / 2f, (size.height - STAGE_H * scale) / 2f)
    return scale to origin
}

private fun Offset.toIntOffset() = IntOffset(x.roundToInt(), y.roundToInt())
