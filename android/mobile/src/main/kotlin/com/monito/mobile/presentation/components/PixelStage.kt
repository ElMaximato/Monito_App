package com.monito.mobile.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform

/** Parte del escenario que realmente se ve en pantalla (en unidades del escenario). */
class StageInfo(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

/** Segundos transcurridos, actualizados en cada frame. Léelo SOLO dentro de bloques de dibujo. */
@Composable
fun rememberSeconds(): State<Float> = produceState(initialValue = 0f) {
    var start = -1L
    while (true) {
        withFrameNanos { now ->
            if (start < 0L) start = now
            value = (now - start) / 1_000_000_000f
        }
    }
}

/**
 * Lienzo con un escenario de tamaño fijo (artWidth x artHeight) que se escala para CUBRIR la pantalla
 * (igual que ContentScale.Crop) y queda centrado. Todo lo que dibujes dentro usa las unidades del escenario.
 */
@Composable
fun PixelStage(
    artWidth: Float,
    artHeight: Float,
    modifier: Modifier = Modifier,
    background: Color = Color.Black,
    content: DrawScope.(StageInfo) -> Unit,
) {
    Canvas(modifier = modifier) {
        drawRect(background)
        val s = maxOf(size.width / artWidth, size.height / artHeight)
        val ox = (size.width - artWidth * s) / 2f
        val oy = (size.height - artHeight * s) / 2f
        val info = StageInfo(
            left = -ox / s,
            top = -oy / s,
            right = (size.width - ox) / s,
            bottom = (size.height - oy) / s,
        )
        withTransform({
            translate(ox, oy)
            scale(s, s, Offset.Zero)
        }) {
            content(this, info)
        }
    }
}

// Sin antialias: los rectángulos quedan "pixel perfect", sin costuras entre uno y otro.
private val pixelPaint = Paint().apply { isAntiAlias = false }

fun DrawScope.pixelRect(x: Float, y: Float, w: Float, h: Float, color: Color) {
    drawIntoCanvas { canvas ->
        pixelPaint.color = color
        canvas.drawRect(x, y, x + w, y + h, pixelPaint)
    }
}

/** Sprite pequeño definido como texto: cada carácter es una celda y la paleta dice de qué color. */
class PixelSprite(val rows: List<String>, val palette: Map<Char, Color>) {
    val columns: Int = rows.maxOf { it.length }
    val lines: Int = rows.size
}

fun DrawScope.drawPixelSprite(sprite: PixelSprite, x: Float, y: Float, cell: Float, alpha: Float = 1f) {
    sprite.rows.forEachIndexed { r, row ->
        row.forEachIndexed { c, ch ->
            val color = sprite.palette[ch]
            if (color != null) {
                pixelRect(x + c * cell, y + r * cell, cell, cell, color.copy(alpha = color.alpha * alpha))
            }
        }
    }
}

/** Destello de 4 puntas. (x, y) es la esquina de la celda central. */
fun DrawScope.drawSparkle(x: Float, y: Float, unit: Float, arm: Int, color: Color) {
    pixelRect(x, y, unit, unit, color)
    for (i in 1..arm) {
        val fade = 1f - (i - 1) / arm.toFloat() * 0.65f
        val c = color.copy(alpha = color.alpha * fade)
        pixelRect(x - i * unit, y, unit, unit, c)
        pixelRect(x + i * unit, y, unit, unit, c)
        pixelRect(x, y - i * unit, unit, unit, c)
        pixelRect(x, y + i * unit, unit, unit, c)
    }
}

/** Módulo que siempre da un valor positivo (sirve para que los objetos "den la vuelta"). */
fun wrap(value: Float, period: Float): Float {
    val m = value % period
    return if (m < 0f) m + period else m
}
