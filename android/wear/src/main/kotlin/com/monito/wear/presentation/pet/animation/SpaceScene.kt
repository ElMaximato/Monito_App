package com.monito.wear.presentation.pet.animation

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Fondo del reloj: cielo con estrellas que titilan y se desplazan muy despacio, una estrella fugaz,
 * rocas y un satélite flotando, y un suelo de Luna con cráteres. Kotlin puro (se prueba en src/test).
 * Todo en coordenadas del escenario del monito (390 x 629); x e y pueden salirse del escenario
 * porque la pantalla del reloj es más ancha que el escenario.
 */
object SpaceScene {
    /** Color del cielo (el mismo del hub del móvil). También lo usa PetScenes para tapar partes del cuerpo. */
    const val SKY = 0xFF11182AL

    /** Altura del suelo en el centro: los pies del monito terminan en y = 551, así que quedan hundidos un poco en la Luna. */
    const val GROUND_TOP = 538f

    private const val G = 6f
    private const val CURVE = 0.00035f   // qué tanto baja el suelo hacia los lados (horizonte curvo)
    private const val CENTER_X = 195f
    private const val WORLD_X0 = -170f
    private const val WORLD_W = 740f

    private const val MOON_RIM = 0xFFC9CEDAL
    private const val MOON_LIGHT = 0xFFA3A9BAL
    private const val MOON_MID = 0xFF868CA2L
    private const val MOON_DARK = 0xFF6D7390L
    private const val CRATER = 0xFF5A607CL

    private class Star(
        val x: Float, val y: Float, val size: Float, val phase: Float, val speed: Float,
        val sparkle: Boolean, val tint: Long,
    )

    private val stars: List<Star> = run {
        val rnd = Random(11)
        List(48) { i ->
            Star(
                x = WORLD_X0 + rnd.nextFloat() * WORLD_W,
                y = -30f + rnd.nextFloat() * 575f,
                size = if (rnd.nextInt(6) == 0) 12f else 6f,
                phase = rnd.nextFloat() * 6.28f,
                speed = 0.7f + rnd.nextFloat() * 1.8f,
                sparkle = i % 9 == 0,
                tint = if (rnd.nextInt(3) == 0) 0xFF7FF0E4L else 0xFFE8F1FFL,
            )
        }
    }

    private val craters = listOf(
        floatArrayOf(60f, 602f, 38f, 8f), floatArrayOf(330f, 612f, 50f, 9f), floatArrayOf(200f, 642f, 32f, 6f),
        floatArrayOf(-30f, 640f, 40f, 8f), floatArrayOf(445f, 636f, 36f, 7f),
    )

    private val rockA = listOf("..ccc...", ".cbbbc..", "cbaabbcc", "cbaabbbc", "cbbbbbbc", ".cbbbbc.", "..cccc..")
    private val rockB = listOf(".ccc.", "cbbbc", "cbabc", "cbbbc", ".ccc.")
    private val satellite = listOf("dddd..c..dddd", "dtdt.cac.dtdt", "dddd.cac.dddd", "dtdt.cac.dtdt", "dddd..c..dddd")
    private val rockColors = mapOf('a' to 0xFF96A0B4L, 'b' to 0xFF62708AL, 'c' to 0xFF282E46L)
    private val satColors = mapOf('d' to 0xFF1E3C8CL, 't' to 0xFF5AA0FFL, 'c' to 0xFF282E46L, 'a' to 0xFFE6EBF5L)

    fun groundTop(x: Float): Float = GROUND_TOP + CURVE * (x - CENTER_X) * (x - CENTER_X)

    private fun withAlpha(rgb: Long, a: Int) = (rgb and 0xFFFFFFL) or (a.coerceIn(0, 255).toLong() shl 24)
    private fun q(v: Float, step: Float) = (v / step).roundToInt() * step

    /** Cajas del fondo en el segundo t, para el rectángulo visible [xMin, xMax] x [yMin, yMax] del escenario. */
    fun boxes(t: Float, xMin: Float, xMax: Float, yMin: Float, yMax: Float): List<Box> {
        val out = ArrayList<Box>(200)

        // estrellas: titilan y avanzan hacia la izquierda ~2.5 px por segundo
        for (s in stars) {
            val drift = ((s.x - t * 2.5f - WORLD_X0) % WORLD_W + WORLD_W) % WORLD_W
            val x = q(WORLD_X0 + drift, 3f)
            val y = q(s.y, 3f)
            if (x + s.size < xMin || x > xMax || y + s.size < yMin || y > yMax) continue
            if (y + s.size > groundTop(x) - 4f) continue
            val tw = 0.5f + 0.5f * sin(t * s.speed + s.phase)
            val alpha = if (tw > 0.66f) 255 else if (tw > 0.33f) 170 else 90
            val color = withAlpha(s.tint, alpha)
            out.add(Box(x, y, s.size, s.size, color))
            if (s.sparkle && alpha == 255) {
                out.add(Box(x - s.size, y, 3f * s.size, s.size, color))
                out.add(Box(x, y - s.size, s.size, 3f * s.size, color))
            }
        }

        // estrella fugaz cada 17 s
        val cycle = t % 17f
        if (cycle < 0.9f) {
            val p = cycle / 0.9f
            val k = (t / 17f).toInt() % 3
            val hx = 430f - 60f * k - 330f * p
            val hy = 30f + 40f * k + 170f * p
            for (i in 0..7) {
                val a = (255 * (1f - i / 8f) * (1f - p * 0.4f)).toInt()
                val size = max(3f, 8f - i * 0.6f)
                out.add(Box(q(hx + i * 16f, 3f), q(hy - i * 8.2f, 3f), size, size, withAlpha(0xFFFFFFFFL, a)))
            }
        }

        // rocas y satélite flotando
        bitmap(out, 22f, 62f + sin(t * 0.6f) * 6f, 5f, rockA, rockColors, 0.85f)
        bitmap(out, 374f, 228f + sin(t * 0.5f + 1.4f) * 5f, 5f, rockB, rockColors, 0.85f)
        val satX = 520f - (t * 12f) % 760f
        bitmap(out, satX, 96f + sin(t * 0.8f) * 3f, 5f, satellite, satColors, 1f)

        // suelo de la Luna: un domo que baja hacia los lados
        var y = GROUND_TOP
        while (y < yMax) {
            val half = sqrt((y + G / 2f - GROUND_TOP) / CURVE)
            val x0 = q(max(xMin, CENTER_X - half), G)
            val x1 = q(min(xMax, CENTER_X + half), G)
            if (x1 > x0) {
                val depth = y - GROUND_TOP
                val c = if (depth < 6f) MOON_RIM else if (depth < 30f) MOON_LIGHT else if (depth < 80f) MOON_MID else MOON_DARK
                out.add(Box(x0, y, x1 - x0, G, c))
            }
            y += G
        }
        for (c in craters) {
            val cx = c[0]; val cy = c[1]; val rx = c[2]; val ry = c[3]
            if (cx + rx < xMin || cx - rx > xMax || cy - ry > yMax) continue
            var cyRow = cy - ry
            while (cyRow < cy + ry) {
                val h = (cyRow + G / 2f - cy) / ry
                val half = rx * sqrt(max(0f, 1f - h * h))
                val a = q(cx - half, G)
                val b = q(cx + half, G)
                if (b > a) out.add(Box(a, q(cyRow, G), b - a, G, CRATER))
                cyRow += G
            }
            out.add(Box(q(cx - rx * 0.6f, G), q(cy + ry, G), q(rx * 1.2f, G), 3f, MOON_RIM))
        }
        return out
    }

    private fun bitmap(
        out: MutableList<Box>, x: Float, y: Float, u: Float, rows: List<String>, colors: Map<Char, Long>, alpha: Float,
    ) {
        val a = (255 * alpha).toInt()
        for ((j, row) in rows.withIndex()) {
            var i = 0
            while (i < row.length) {
                val ch = row[i]
                val color = colors[ch]
                if (color != null) {
                    var k = i
                    while (k < row.length && row[k] == ch) k++
                    out.add(Box(q(x + i * u, 1f), q(y + j * u, 1f), (k - i) * u, u, withAlpha(color, a)))
                    i = k
                } else i++
            }
        }
    }
}
