package com.monito.mobile.presentation.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// Escenario de 120 x 270 "pixeles de arte" (coincide con tools/scenes/build_scene_assets.py).
private const val W = 120f
private const val H = 270f
private val WIN_CENTER = Offset(60f, 84f)
private const val WIN_RADIUS = 41.5f

private val SpaceTop = Color(0xFF05060F)
private val SpaceBottom = Color(0xFF0B1230)
private val Orange = Color(0xFFF68E2D)
private val Teal = Color(0xFF2EE6D0)
private val Pink = Color(0xFFE8829B)

private val SatPalette = mapOf(
    'd' to Color(0xFF1E3C8C), 't' to Color(0xFF5AA0FF), 'c' to Color(0xFF282E46), 'a' to Color(0xFFE6EBF5),
)
private val MiniSat = PixelSprite(listOf("dd.c.dd", "dtcacdt", "dd.c.dd"), SatPalette)
private val MiniRock = PixelSprite(
    listOf(".cc.", "cbbc", "cabc", ".cc."),
    mapOf('a' to Color(0xFF96A0B4), 'b' to Color(0xFF62708A), 'c' to Color(0xFF282E46)),
)

private class WinStar(val x: Float, val y: Float, val phase: Float, val speed: Float, val big: Boolean)
private class Mote(val x: Float, val y: Float, val speed: Float, val phase: Float, val big: Boolean)

private fun buildWinStars(): List<WinStar> {
    val rnd = Random(21)
    return List(46) {
        WinStar(
            x = rnd.nextFloat() * 90f,
            y = 40f + rnd.nextFloat() * 90f,
            phase = rnd.nextFloat() * 6.28f,
            speed = 0.8f + rnd.nextFloat() * 2f,
            big = it % 12 == 0,
        )
    }
}

private fun buildMotes(): List<Mote> {
    val rnd = Random(9)
    return List(30) {
        Mote(
            x = 4f + rnd.nextFloat() * 112f,
            y = rnd.nextFloat() * 200f,
            speed = 1.5f + rnd.nextFloat() * 3.5f,
            phase = rnd.nextFloat() * 6.28f,
            big = it % 7 == 0,
        )
    }
}

private val SparklePositions = listOf(
    Offset(12f, 52f), Offset(105f, 62f), Offset(20f, 126f), Offset(100f, 122f), Offset(60f, 24f),
)

/**
 * Escena del perfil: el monito, de espaldas, mirando la Tierra desde una ventana de la nave.
 * Capas: espacio (tras el cristal) -> pared con ventana -> reflejos -> luz -> monito -> polvo y destellos.
 */
@Composable
fun ProfileScene(modifier: Modifier = Modifier) {
    val windowImg = ImageBitmap.imageResource(R.drawable.profile_window)
    val earthImg = ImageBitmap.imageResource(R.drawable.profile_earth)
    val robotImg = ImageBitmap.imageResource(R.drawable.profile_robot_back)
    val time = rememberSeconds()
    val winStars = remember { buildWinStars() }
    val motes = remember { buildMotes() }
    val glass = remember {
        Path().apply { addOval(Rect(center = WIN_CENTER, radius = WIN_RADIUS)) }
    }

    PixelStage(
        artWidth = W,
        artHeight = H,
        modifier = modifier
            .clipToBounds()
            .graphicsLayer {
                // cámara que avanza muy despacio, como en un ending de anime
                val k = 1f + 0.035f * (0.5f - 0.5f * cos(time.value * 0.12f))
                scaleX = k
                scaleY = k
                transformOrigin = TransformOrigin(0.5f, 0.3f)
            },
        background = Color(0xFF0A0C18),
    ) { info ->
        val t = time.value

        // 1) espacio detrás del cristal
        clipPath(glass) {
            drawRect(
                brush = Brush.verticalGradient(listOf(SpaceTop, SpaceBottom), startY = 40f, endY = 130f),
                topLeft = Offset(15f, 35f),
                size = Size(90f, 100f),
            )
            drawWindowStars(winStars, t)
            // halo y Tierra
            val ex = 60f + sin(t * 0.15f) * 3f
            val ey = 80f + sin(t * 0.21f) * 1.5f
            drawCircle(
                color = Color(0.3f, 0.6f, 1f, 0.10f + 0.05f * sin(t * 0.8f)),
                radius = 40f,
                center = Offset(ex, ey),
            )
            drawImage(
                image = earthImg,
                dstOffset = IntOffset((ex - 36f).roundToInt(), (ey - 36f).roundToInt()),
                dstSize = IntSize(72, 72),
                filterQuality = FilterQuality.None,
            )
            drawWindowTraffic(t)
        }

        // 2) pared + marco de la ventana
        drawImage(
            image = windowImg,
            dstSize = IntSize(windowImg.width, windowImg.height),
            filterQuality = FilterQuality.None,
        )
        drawLamp(t)
        drawConsoleLeds(t)

        // 3) reflejos en el cristal
        clipPath(glass) {
            val sweep = wrap(t * 4f, 150f) - 30f
            rotate(degrees = -28f, pivot = WIN_CENTER) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Transparent,
                        0.5f to Color(1f, 1f, 1f, 0.10f),
                        1f to Color.Transparent,
                        startX = sweep,
                        endX = sweep + 14f,
                    ),
                    topLeft = Offset(sweep, 20f),
                    size = Size(14f, 130f),
                )
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Transparent,
                        0.5f to Color(1f, 1f, 1f, 0.06f),
                        1f to Color.Transparent,
                        startX = sweep + 18f,
                        endX = sweep + 24f,
                    ),
                    topLeft = Offset(sweep + 18f, 20f),
                    size = Size(6f, 130f),
                )
            }
        }

        // 4) luz de la ventana sobre la cabina
        val pulse = 0.85f + 0.15f * sin(t * 0.7f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0.35f, 0.6f, 1f, 0.24f * pulse), Color(0.35f, 0.6f, 1f, 0f)),
                center = WIN_CENTER,
                radius = 120f,
            ),
            radius = 120f,
            center = WIN_CENTER,
        )

        // 5) el monito, flotando en gravedad cero
        val bob = (sin(t * 1.3f) * 1.6f).roundToInt()
        val sway = (sin(t * 0.5f) * 1.2f).roundToInt()
        val rx = (60 - robotImg.width / 2 + sway)
        val ry = 92 + bob
        val rim = ColorFilter.tint(Color(0.45f, 0.85f, 1f, 1f), BlendMode.SrcIn)
        drawImage(robotImg, dstOffset = IntOffset(rx - 1, ry - 1), alpha = 0.45f, colorFilter = rim,
            dstSize = IntSize(robotImg.width, robotImg.height), filterQuality = FilterQuality.None)
        drawImage(
            image = robotImg,
            dstOffset = IntOffset(rx, ry),
            dstSize = IntSize(robotImg.width, robotImg.height),
            colorFilter = ColorFilter.tint(Color(0.45f, 0.7f, 1f, 0.14f), BlendMode.SrcAtop),
            filterQuality = FilterQuality.None,
        )

        // 6) polvo flotando y destellos
        drawMotes(motes, t)
        SparklePositions.forEachIndexed { i, p ->
            val a = max(0f, sin(t * (0.9f + i * 0.17f) + i * 1.7f))
            if (a > 0.05f) drawSparkle(p.x, p.y, 1f, if (a > 0.7f) 3 else 2, Color(0.85f, 0.95f, 1f, a * 0.9f))
        }

        // 7) degradado inferior (para que se lea el texto) y viñeta
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                1f to Color(0.04f, 0.05f, 0.1f, 0.94f),
                startY = 120f,
                endY = 215f,
            ),
            topLeft = Offset(info.left, 120f),
            size = Size(info.width, info.bottom - 120f),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0.02f, 0.03f, 0.07f, 0.6f)),
                center = Offset(60f, 110f),
                radius = 150f,
            ),
            topLeft = Offset(info.left, info.top),
            size = Size(info.width, info.height),
        )
    }
}

private fun DrawScope.drawWindowStars(stars: List<WinStar>, t: Float) {
    stars.forEach { s ->
        val x = 15f + wrap(s.x - t * 0.7f, 90f)
        val a = 0.15f + 0.85f * (0.5f + 0.5f * sin(t * s.speed + s.phase))
        val c = Color(0.9f, 0.96f, 1f, a)
        if (s.big) drawSparkle(x.toInt().toFloat(), s.y.toInt().toFloat(), 1f, 2, c)
        else pixelRect(x.toInt().toFloat(), s.y.toInt().toFloat(), 1f, 1f, c)
    }
}

/** Un satélite y una roca que cruzan la ventana, y una estrella fugaz cada cierto tiempo. */
private fun DrawScope.drawWindowTraffic(t: Float) {
    val satX = wrap(t * 1.6f, 130f) - 20f
    drawPixelSprite(MiniSat, satX.toInt().toFloat(), 56f, 1f)
    if (sin(t * 4f) > 0f) pixelRect(satX.toInt() + 3f, 55f, 1f, 1f, Orange)

    val rockX = 125f - wrap(t * 1.1f + 40f, 140f)
    drawPixelSprite(MiniRock, rockX.toInt().toFloat(), 112f, 1f, 0.9f)

    val p = (t % 9f) / 1.0f
    if (p in 0f..1f && t > 2f) {
        val fade = sin(p * PI.toFloat())
        val hx = 104f - p * 72f
        val hy = 46f + p * 52f
        for (k in 0..7) {
            pixelRect((hx + k * 1.4f).toInt().toFloat(), (hy - k * 0.9f).toInt().toFloat(), 1f, 1f,
                Color(0.75f, 1f, 0.95f, fade * (1f - k / 8f)))
        }
        pixelRect(hx.toInt().toFloat(), hy.toInt().toFloat(), 2f, 1f, Color(1f, 1f, 1f, fade))
    }
}

private fun DrawScope.drawLamp(t: Float) {
    val flicker = 0.85f + 0.15f * sin(t * 1.7f)
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(1f, 0.85f, 0.6f, 0.20f * flicker), Color(1f, 0.85f, 0.6f, 0f)),
            startY = 7f,
            endY = 36f,
        ),
        topLeft = Offset(12f, 7f),
        size = Size(96f, 30f),
    )
}

private fun DrawScope.drawConsoleLeds(t: Float) {
    val leds = listOf(14f to Orange, 22f to Teal, 98f to Pink, 106f to Orange)
    leds.forEachIndexed { i, (x, color) ->
        if (sin(t * (2.2f + i * 0.7f) + i) > -0.1f) pixelRect(x, 144f, 3f, 3f, color)
    }
}

private fun DrawScope.drawMotes(motes: List<Mote>, t: Float) {
    motes.forEach { m ->
        val y = wrap(m.y - t * m.speed, 200f) + 10f
        val x = m.x + sin(t * 0.5f + m.phase) * 3f
        // más brillantes cerca de la luz de la ventana
        val dist = kotlin.math.hypot(x - WIN_CENTER.x, y - WIN_CENTER.y)
        val glow = (1.2f - dist / 110f).coerceIn(0.15f, 1f)
        val a = glow * (0.4f + 0.5f * sin(t * 1.2f + m.phase))
        val size = if (m.big) 2f else 1f
        pixelRect(x.toInt().toFloat(), y.toInt().toFloat(), size, size, Color(0.8f, 0.92f, 1f, max(a, 0f)))
    }
}
