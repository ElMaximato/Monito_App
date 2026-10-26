package com.monito.mobile.presentation.hub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.monito.mobile.R
import com.monito.mobile.presentation.components.PixelSprite
import com.monito.mobile.presentation.components.PixelStage
import com.monito.mobile.presentation.components.StageInfo
import com.monito.mobile.presentation.components.drawPixelSprite
import com.monito.mobile.presentation.components.drawSparkle
import com.monito.mobile.presentation.components.pixelRect
import com.monito.mobile.presentation.components.rememberSeconds
import com.monito.mobile.presentation.components.wrap
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// El escenario mide 1024x1024 (igual que la imagen original) y 1 "pixel" del arte = 4 unidades.
private const val STAGE = 1024f
private const val U = 4f

private val SkyColor = Color(0xFF11182A)
private val EyeColor = Color(0xFF40E8DA)
private val EyeShine = Color(0xFFB4FFF6)

private const val EARTH_X = 291f
private const val EARTH_Y = 28f
private const val EARTH_SIZE = 452f
private const val FRONT_Y = 500f

private val rockPalette = mapOf('a' to Color(0xFF96A0B4), 'b' to Color(0xFF62708A), 'c' to Color(0xFF282E46))
private val satPalette = mapOf(
    'd' to Color(0xFF1E3C8C), 't' to Color(0xFF5AA0FF), 'c' to Color(0xFF282E46), 'a' to Color(0xFFE6EBF5),
)

private val RockA = PixelSprite(
    listOf("..ccc...", ".cbbbc..", "cbaabbcc", "cbaabbbc", "cbbbbbbc", ".cbbbbc.", "..cccc.."), rockPalette,
)
private val RockB = PixelSprite(listOf(".ccc.", "cbbbc", "cbabc", "cbbbc", ".ccc."), rockPalette)
private val Satellite = PixelSprite(
    listOf("dddd..c..dddd", "dtdt.cac.dtdt", "dddd.cac.dddd", "dtdt.cac.dtdt", "dddd..c..dddd"), satPalette,
)

private class Star(
    val x: Float, val y: Float, val size: Float, val phase: Float, val speed: Float,
    val sparkle: Boolean, val tint: Color,
)

private class Floater(
    val sprite: PixelSprite, val cell: Float, val y: Float, val speed: Float, val offset: Float,
    val bobAmp: Float, val bobSpeed: Float, val phase: Float, val alpha: Float,
)

private class Mote(val x: Float, val y: Float, val speed: Float, val phase: Float)

private fun buildStars(): List<Star> {
    val rnd = Random(11)
    return List(170) { i ->
        Star(
            x = rnd.nextInt(0, 256) * U,
            y = rnd.nextInt(0, 175) * U,
            size = if (rnd.nextInt(5) == 0) 2 * U else U,
            phase = rnd.nextFloat() * 6.28f,
            speed = 0.7f + rnd.nextFloat() * 1.8f,
            sparkle = i % 10 == 0,
            tint = if (rnd.nextInt(3) == 0) Color(0xFF7FF0E4) else Color(0xFFE8F1FF),
        )
    }
}

private fun buildFloaters(): List<Floater> = listOf(
    Floater(RockA, 4f, 130f, 14f, 100f, 6f, 0.6f, 0.3f, 0.55f),
    Floater(RockB, 4f, 330f, -10f, 400f, 5f, 0.5f, 1.4f, 0.6f),
    Floater(RockB, 8f, 80f, 9f, 700f, 8f, 0.4f, 2.2f, 0.75f),
    Floater(Satellite, 5f, 215f, 22f, 0f, 4f, 0.8f, 0.9f, 1f),
    Floater(RockA, 8f, 410f, -6f, 250f, 10f, 0.35f, 3.1f, 1f),
    Floater(RockB, 4f, 440f, 18f, 600f, 4f, 0.9f, 4.0f, 0.85f),
)

private fun buildMotes(): List<Mote> {
    val rnd = Random(5)
    return List(16) {
        Mote(
            x = 330f + rnd.nextFloat() * 360f,
            y = 600f + rnd.nextFloat() * 320f,
            speed = 18f + rnd.nextFloat() * 22f,
            phase = rnd.nextFloat() * 6.28f,
        )
    }
}

/** Fondo vivo del hub: estrellas que titilan, objetos flotando, la Tierra respirando y el monito parpadeando. */
@Composable
fun HubScene(modifier: Modifier = Modifier) {
    val earth = ImageBitmap.imageResource(R.drawable.hub_earth)
    val front = ImageBitmap.imageResource(R.drawable.hub_front)
    val time = rememberSeconds()
    val stars = remember { buildStars() }
    val floaters = remember { buildFloaters() }
    val motes = remember { buildMotes() }

    PixelStage(artWidth = STAGE, artHeight = STAGE, modifier = modifier, background = SkyColor) { info ->
        val t = time.value

        drawStars(stars, t)
        drawFloaters(floaters, t, info, behindEarth = true)

        // Tierra: flota suavemente y su halo "respira"
        val bob = sin(t * 0.6f) * 6f
        val glowAlpha = 0.65f + 0.35f * sin(t * 0.9f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0.24f, 0.55f, 1f, 0.38f), Color(0.24f, 0.55f, 1f, 0f)),
                center = Offset(517f, 254f + bob),
                radius = 340f,
            ),
            radius = 340f,
            center = Offset(517f, 254f + bob),
            alpha = glowAlpha,
        )
        drawImage(
            image = earth,
            dstOffset = IntOffset(EARTH_X.roundToInt(), (EARTH_Y + bob).roundToInt()),
            dstSize = IntSize(EARTH_SIZE.roundToInt(), EARTH_SIZE.roundToInt()),
            filterQuality = FilterQuality.None,
        )

        drawFloaters(floaters, t, info, behindEarth = false)
        drawShootingStar(t, info)

        // Rocas + astronauta (sin ojos, se dibujan abajo)
        drawImage(
            image = front,
            dstOffset = IntOffset(0, FRONT_Y.roundToInt()),
            dstSize = IntSize(front.width, front.height),
            filterQuality = FilterQuality.None,
        )
        drawEyes(t)
        drawMotes(motes, t)

        // viñeta suave
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0.04f, 0.05f, 0.1f, 0.55f)),
                center = Offset(512f, 560f),
                radius = 820f,
            ),
            topLeft = Offset(info.left, info.top),
            size = Size(info.width, info.height),
        )
    }
}

private fun DrawScope.drawStars(stars: List<Star>, t: Float) {
    stars.forEach { s ->
        val a = 0.2f + 0.8f * (0.5f + 0.5f * sin(t * s.speed + s.phase))
        val color = s.tint.copy(alpha = a)
        if (s.sparkle) {
            drawSparkle(s.x, s.y, U, if (a > 0.75f) 3 else 2, color)
        } else {
            pixelRect(s.x, s.y, s.size, s.size, color)
        }
    }
}

private fun DrawScope.drawFloaters(list: List<Floater>, t: Float, info: StageInfo, behindEarth: Boolean) {
    list.forEach { f ->
        // los objetos lejanos (alpha bajo) van detrás de la Tierra, los cercanos delante
        if ((f.alpha < 0.8f) != behindEarth) return@forEach
        val w = f.sprite.columns * f.cell
        val margin = w + 40f
        val span = info.width + 2 * margin
        val x = info.left - margin + wrap(f.offset + f.speed * t, span)
        val y = f.y + sin(t * f.bobSpeed + f.phase) * f.bobAmp
        val gx = (x / U).roundToInt() * U
        val gy = (y / U).roundToInt() * U
        drawPixelSprite(f.sprite, gx, gy, f.cell, f.alpha)
        if (f.sprite === Satellite && sin(t * 5f) > 0f) {
            pixelRect(gx + 6 * f.cell, gy - f.cell, f.cell, f.cell, Color(0xFFF68E2D))
        }
    }
}

private fun DrawScope.drawShootingStar(t: Float, info: StageInfo) {
    val cycle = 8f
    val p = (t % cycle) / 1.2f          // dura 1.2 s cada 8 s
    if (p > 1f || t < 3f) return
    val startX = info.right - 20f
    val hx = startX - p * 560f
    val hy = 70f + p * 280f
    val fade = sin(p * PI.toFloat())
    for (k in 0..11) {
        val tx = hx + k * 14f
        val ty = hy - k * 7f
        val a = fade * (1f - k / 12f)
        pixelRect(((tx / U).roundToInt() * U), ((ty / U).roundToInt() * U), U, U, Color(0.7f, 1f, 0.95f, a * 0.8f))
    }
    pixelRect(((hx / U).roundToInt() * U), ((hy / U).roundToInt() * U), 2 * U, 2 * U, Color(1f, 1f, 1f, fade))
}

private fun DrawScope.drawEyes(t: Float) {
    // mirada: centro -> izquierda -> derecha -> centro (ciclo de 10 s)
    val c = t % 10f
    val glanceX = when {
        c in 3f..4.6f -> -8f
        c in 6.4f..8f -> 8f
        else -> 0f
    }
    val glanceY = if (c in 4.6f..5.2f) -4f else 0f
    // parpadeo cada 4.6 s (y uno doble de vez en cuando)
    val b = t % 4.6f
    val doubleBlink = (t / 4.6f).toInt() % 3 == 0 && b in 0.3f..0.43f
    val closed = b < 0.13f || doubleBlink
    val h = if (closed) 6f else 28f
    val top = 634f + (28f - h) / 2f + glanceY
    for (ex in floatArrayOf(458f, 549f)) {
        pixelRect(ex + glanceX, top, 28f, h, EyeColor)
        if (!closed) pixelRect(ex + glanceX + 4f, top + 4f, 8f, 8f, EyeShine)
    }
}

private fun DrawScope.drawMotes(motes: List<Mote>, t: Float) {
    motes.forEach { m ->
        val progress = wrap(t * m.speed + m.phase * 40f, 300f) / 300f
        val y = m.y - progress * 300f
        val x = m.x + sin(t * 0.8f + m.phase) * 14f
        val a = sin(progress * PI.toFloat()) * 0.7f
        pixelRect(((x / U).roundToInt() * U), ((y / U).roundToInt() * U), U, U, Color(0.7f, 1f, 0.95f, max(a, 0f)))
    }
}
