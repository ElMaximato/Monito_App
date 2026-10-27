package com.monito.wear.presentation.pet.animation

import com.monito.shared.state.PetState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Escenas del monito: cuerpo fijo (astro_base / astro_sit) + ojos, boca y accesorios dibujados en código.
 * Es Kotlin puro (sin Compose) para poder probarlo en src/test. Todo está en coordenadas del escenario
 * (390 x 629) y en ARGB. `t` son los segundos desde que empezó el estado: la intro ocurre UNA vez
 * (por ejemplo, sacar el paraguas) y después la escena se queda en su pose final con las partes que sí
 * se repiten (lluvia cayendo, abanico moviéndose, etc.).
 */

class Box(val x: Float, val y: Float, val w: Float, val h: Float, val color: Long)

enum class Eyes { OPEN, CLOSED, HAPPY, SMILE, TIRED, SQUINT, STAR }
enum class Mouth { NONE, SMALL, YAWN_S, YAWN_L, HAPPY }

/** Imagen de cuerpo y dónde van los ojos sobre ella. legsY = desde esa fila el cuerpo no se mueve al respirar. */
enum class Body(
    val legsY: Int,
    val eyeLeftX: Float, val eyeRightX: Float, val eyeY: Float, val eyeW: Float, val eyeH: Float,
) {
    STANDING(487, 142f, 232f, 244f, 16f, 38f),
    SITTING(436, 136f, 232f, 251f, 20f, 30f),
}

/**
 * Un cuadro de una escena. Orden de dibujo: behind -> cuerpo -> erase -> face -> front.
 * bobY/hopY/dx mueven el cuerpo; los accesorios "pegados" ya vienen desplazados.
 */
class SceneFrame(
    val body: Body,
    val bobY: Float,
    val hopY: Float,
    val dx: Float,
    val behind: List<Box>,
    val erase: List<Box>,
    val face: List<Box>,
    val front: List<Box>,
)

private object C {
    const val BG = 0xFF000000L          // fondo de la pantalla del reloj
    const val OUTLINE = 0xFF201D38L
    const val CREAM = 0xFFFCF3E4L
    const val VISOR_SIT = 0xFF353C56L
    const val EYE = 0xFF40E8DAL
    const val SHINE = 0xFFB4FFF6L
    const val LID = 0xFF9FB6BFL
    const val MOUTH = 0xFFFF7FA0L
    const val TONGUE = 0xFFC9507AL
    const val BLUSH = 0xFFFF8FB0L
    const val RAIN = 0xFF7FD6FFL
    const val RAIN2 = 0xFFBFE9FFL
    const val PUDDLE = 0xFF1B3556L
    const val UMB1 = 0xFFF28C38L
    const val UMB2 = 0xFFFCF3E4L
    const val POLE = 0xFFD9C7B0L
    const val FAN1 = 0xFFFF9A3CL
    const val FAN2 = 0xFFFFD08AL
    const val FANR = 0xFFD96A1EL
    const val SWEAT = 0xFF7FD6FFL
    const val HEAT = 0xFFFFC27AL
    const val SCARF = 0xFFE8546AL
    const val SCARF2 = 0xFFFFB3C0L
    const val MITT = 0xFFF28C38L
    const val SLEEVE = 0xFFC9A98CL
    const val SNOW = 0xFFBFE9FFL
    const val HEART = 0xFFFF6E9CL
    const val ZZZ = 0xFF9FB6FFL
    const val CAP = 0xFF5B6FD6L
    const val GOLD = 0xFFFFE66EL
}

private const val G = 6f // un "píxel" del arte

private fun q(v: Float) = (v / G).roundToInt() * G
private fun ramp(t: Float, start: Float, dur: Float) = ((t - start) / dur).coerceIn(0f, 1f)
private fun stepped(r: Float, n: Int) = floor(r * n) / n
private fun breath(t: Float, speed: Float = 1.6f) = if (sin(t * speed) > 0.15f) G else 0f
private fun blink(t: Float, open: Eyes = Eyes.OPEN) = if (t % 4.6f < 0.13f) Eyes.CLOSED else open
private fun lookX(t: Float): Float {
    val c = t % 10f
    return when { c in 3f..4.6f -> -12f; c in 6.4f..8f -> 12f; else -> 0f }
}
private fun withAlpha(rgb: Long, a: Int) = (rgb and 0xFFFFFFL) or (a.coerceIn(0, 255).toLong() shl 24)

/** Pinta cajas en una lista, con un desplazamiento (para lo que va pegado al cuerpo). */
private class Layer(val out: MutableList<Box>, val ox: Float = 0f, val oy: Float = 0f) {
    fun rect(x: Float, y: Float, w: Float, h: Float, c: Long, snap: Boolean = true) {
        val px = x + ox
        val py = y + oy
        if (snap) out.add(Box(q(px), q(py), max(G, q(x + w + ox) - q(px)), max(G, q(h)), c))
        else out.add(Box(px, py, w, h, c))
    }

    /** Dibuja de x0 a x1 en una fila de alto G (los bordes se alinean a la cuadrícula). */
    fun span(x0: Float, x1: Float, y: Float, c: Long, h: Float = G) {
        val a = q(x0 + ox)
        val b = q(x1 + ox)
        if (b > a) out.add(Box(a, q(y + oy), b - a, h, c))
    }

    /** Píxel-art desde texto: cada X es un cuadrito de lado u. */
    fun bitmap(x: Float, y: Float, u: Float, rows: List<String>, c: Long) {
        for ((j, row) in rows.withIndex()) {
            var i = 0
            while (i < row.length) {
                if (row[i] == 'X') {
                    var k = i
                    while (k < row.length && row[k] == 'X') k++
                    rect(x + i * u, y + j * u, (k - i) * u, u, c, snap = false)
                    i = k
                } else i++
            }
        }
    }

    fun ellipse(cx: Float, cy: Float, rx: Float, ry: Float, c: Long) {
        var y = -ry
        while (y < ry) {
            val h = (y + G / 2f) / ry
            val half = rx * sqrt(max(0f, 1f - h * h))
            span(cx - half, cx + half, cy + y, c)
            y += G
        }
    }

    /** Cúpula de paraguas con franjas. La base plana está en cy. */
    fun dome(cx: Float, cy: Float, rx: Float, ry: Float, c1: Long, c2: Long, bands: Int) {
        val bandW = 2f * rx / bands
        var y = cy - ry
        while (y < cy) {
            val h = (cy - (y + G / 2f)) / ry
            val half = rx * sqrt(max(0f, 1f - h * h))
            for (b in 0 until bands) {
                val b0 = max(cx - rx + b * bandW, cx - half)
                val b1 = minOf(cx - rx + (b + 1) * bandW, cx + half)
                if (b1 > b0) span(b0, b1, y, if (b % 2 == 0) c1 else c2)
            }
            y += G
        }
        for (b in 0 until bands step 2) span(cx - rx + b * bandW, cx - rx + (b + 1) * bandW, cy, c1)
    }
}

private class Parts(dx: Float, dy: Float) {
    val behind = ArrayList<Box>()
    val erase = ArrayList<Box>()
    val front = ArrayList<Box>()
    val bgAbs = Layer(behind)
    val bgAtt = Layer(behind, dx, dy)
    val eraseAtt = Layer(erase, dx, dy)
    val fgAbs = Layer(front)
    val fgAtt = Layer(front, dx, dy)
}

private val HEART = listOf(".XX.XX.", "XXXXXXX", "XXXXXXX", ".XXXXX.", "..XXX..", "...X...")
private val SPARK = listOf("..X..", "..X..", "XXXXX", "..X..", "..X..")
private val FLAKE = listOf(".X.", "XXX", ".X.")
private val DROP = listOf("..X..", ".XXX.", "XXXXX", "XXXXX", ".XXX.")
private val ZED = listOf("XXXX", "..X.", ".X..", "XXXX")
private val CHEV_RIGHT = listOf("XX...", ".XX..", "..XX.", ".XX..", "XX...")
private val CHEV_LEFT = listOf("...XX", "..XX.", ".XX..", "..XX.", "...XX")
private val STAR_EYE = listOf("...X...", "...X...", "..XXX..", "XXXXXXX", "..XXX..", "...X...", "...X...")

// ---------- cara ----------

private fun faceBoxes(body: Body, eyes: Eyes, mouth: Mouth, gx: Float, gy: Float, ox: Float, oy: Float): List<Box> {
    val out = ArrayList<Box>()
    val l = Layer(out, ox, oy)
    for ((i, ex0) in floatArrayOf(body.eyeLeftX, body.eyeRightX).withIndex()) {
        val ex = ex0 + gx
        val ey = body.eyeY + gy
        val w = body.eyeW
        val h = body.eyeH
        val cx = ex + w / 2f
        val cy = ey + h / 2f
        val left = i == 0
        fun r(x: Float, y: Float, rw: Float, rh: Float, c: Long = C.EYE) = l.rect(x, y, rw, rh, c, snap = false)
        when (eyes) {
            Eyes.OPEN -> { r(ex, ey, w, h); r(ex + 3f, ey + 4f, 6f, 6f, C.SHINE) }
            Eyes.CLOSED -> r(ex, cy - 4f, w, 8f)
            Eyes.HAPPY -> { r(cx - 6f, cy - 8f, 12f, 8f); r(cx - 14f, cy, 10f, 8f); r(cx + 4f, cy, 10f, 8f) }
            Eyes.SMILE -> { r(cx - 14f, cy - 8f, 10f, 8f); r(cx - 6f, cy, 12f, 8f); r(cx + 4f, cy - 8f, 10f, 8f) }
            Eyes.TIRED -> { r(ex, cy, w, h / 2f); r(ex - 3f, cy - 6f, w + 6f, 6f, C.LID) }
            Eyes.SQUINT -> l.bitmap(cx - 15f, cy - 15f, 6f, if (left) CHEV_RIGHT else CHEV_LEFT, C.EYE)
            Eyes.STAR -> l.bitmap(cx - 21f, cy - 21f, 6f, STAR_EYE, C.SHINE)
        }
    }
    val mx = 195f + gx / 2f
    val my = body.eyeY + body.eyeH + 14f + gy
    fun m(x: Float, y: Float, w: Float, h: Float, c: Long = C.MOUTH) = l.rect(mx + x, my + y, w, h, c, snap = false)
    when (mouth) {
        Mouth.NONE -> {}
        Mouth.SMALL -> m(-9f, 0f, 18f, 6f)
        Mouth.YAWN_S -> m(-12f, 0f, 24f, 12f)
        Mouth.YAWN_L -> { m(-15f, 0f, 30f, 24f); m(-9f, 12f, 18f, 12f, C.TONGUE) }
        Mouth.HAPPY -> { m(-18f, 0f, 36f, 6f); m(-12f, 6f, 24f, 6f); m(-6f, 12f, 12f, 6f, C.TONGUE) }
    }
    return out
}

private fun scene(
    body: Body = Body.STANDING, bob: Float = 0f, hop: Float = 0f, dx: Float = 0f,
    eyes: Eyes = Eyes.OPEN, mouth: Mouth = Mouth.NONE, gx: Float = 0f, gy: Float = 0f,
    blush: Boolean = false,
    build: Parts.() -> Unit = {},
): SceneFrame {
    val p = Parts(dx, bob + hop)
    if (body == Body.SITTING) {
        // El sprite sentado trae los ojos dibujados: los tapamos con el color del visor
        p.eraseAtt.rect(126f, 244f, 42f, 52f, C.VISOR_SIT, snap = false)
        p.eraseAtt.rect(223f, 244f, 42f, 52f, C.VISOR_SIT, snap = false)
    }
    p.build()
    val face = faceBoxes(body, eyes, mouth, gx, gy, dx, bob + hop).toMutableList()
    if (blush) {
        val l = Layer(face, dx, bob + hop)
        l.rect(112f, 296f, 30f, 8f, C.BLUSH, snap = false)
        l.rect(248f, 296f, 30f, 8f, C.BLUSH, snap = false)
    }
    return SceneFrame(body, bob, hop, dx, p.behind, p.erase, face, p.front)
}

object PetScenes {
    /** Estados que tienen escena (los demás siguen usando su hoja de sprites o su imagen fija). */
    fun handles(state: PetState) = frame(state, 0f) != null

    /** La escena de un estado en el segundo t desde que empezó, o null si ese estado usa su sprite normal. */
    fun frame(state: PetState, t: Float): SceneFrame? = when (state) {
        PetState.IDLE -> idle(t)
        PetState.BORED -> bored(t)
        PetState.NIGHT -> night(t)
        PetState.CALM -> calm(t)
        PetState.EXCITED -> excited(t)
        PetState.PET -> pet(t)
        PetState.RAIN -> rain(t)
        PetState.HOT -> hot(t)
        PetState.COLD -> cold(t)
        else -> null
    }

    private fun idle(t: Float): SceneFrame {
        val f = IdleMotion.frame(t)
        val eyes = when (f.eyes) {
            EyeStyle.OPEN -> Eyes.OPEN
            EyeStyle.CLOSED -> Eyes.CLOSED
            EyeStyle.HAPPY -> Eyes.HAPPY
        }
        return scene(bob = f.bobY, hop = f.hopY, eyes = eyes, gx = f.glanceX, gy = f.glanceY)
    }

    // ---------- aburrido: sentado, mira alrededor y bosteza de vez en cuando ----------
    private fun bored(t: Float): SceneFrame {
        val c = t % 9f
        val yawn = c in 5.5f..7.6f
        val p = c - 5.5f
        val mouth = when {
            !yawn -> Mouth.NONE
            p < 0.4f -> Mouth.YAWN_S
            p < 1.6f -> Mouth.YAWN_L
            else -> Mouth.YAWN_S
        }
        val eyes = when {
            yawn -> Eyes.SQUINT
            t % 5.3f < 0.25f -> Eyes.CLOSED
            else -> Eyes.TIRED
        }
        val bob = if (yawn) 0f else if (sin(t * 1.1f) > 0.2f) G else 0f
        return scene(body = Body.SITTING, bob = bob, eyes = eyes, mouth = mouth,
            gx = if (yawn) 0f else lookX(t) * 0.6f) {
            // "..." sobre la cabeza: aparece un punto cada segundo
            val dots = (t % 4f).toInt()
            for (i in 0 until dots) fgAtt.rect(262f + i * 22f, 96f, 10f, 10f, C.ZZZ, snap = false)
        }
    }

    // ---------- noche: duerme con gorro y zZz ----------
    private fun night(t: Float): SceneFrame {
        val bob = if (sin(t * 0.9f) > 0f) G else 0f
        return scene(bob = bob, eyes = Eyes.SMILE) {
            // gorro: rodete claro sobre el casco, cuerpo azul que se inclina y cae a la derecha, pompón
            fgAtt.span(100f, 292f, 112f, C.CREAM)
            fgAtt.span(100f, 292f, 118f, C.CREAM)
            var y = 106f
            while (y >= 64f) {
                val k = (106f - y) / 42f
                fgAtt.span(112f + 118f * k, 286f + 14f * k, y, C.CAP)
                y -= G
            }
            fgAtt.span(232f, 300f, 58f, C.CAP)
            fgAtt.span(262f, 316f, 64f, C.CAP)
            fgAtt.span(286f, 326f, 70f, C.CAP)
            fgAtt.span(300f, 334f, 76f, C.CAP)
            fgAtt.span(308f, 336f, 82f, C.CAP)
            fgAtt.rect(318f, 84f, 30f, 30f, C.CREAM)
            for (i in 0 until 3) {
                val ph = (t / 3.2f + i / 3f) % 1f
                val z = C.ZZZ.let { withAlpha(it, (255 * (1f - ph)).toInt() + 30) }
                fgAbs.bitmap(300f + ph * 36f + i * 6f, 190f - ph * 120f, 4f + i * 2f, ZED, z)
            }
            for (i in 0 until 4) {
                val on = sin(t * 2f + i * 1.7f) > -0.2f
                if (on) fgAbs.bitmap(listOf(30f, 346f, 54f, 336f)[i], listOf(70f, 90f, 190f, 215f)[i], 3f, FLAKE, C.GOLD)
            }
        }
    }

    // ---------- tranquilo: ojos contentos, respira lento, corazón que late ----------
    private fun calm(t: Float): SceneFrame {
        val bob = if (sin(t * 0.8f) > 0f) G else 0f
        return scene(bob = bob, eyes = Eyes.SMILE, blush = true) {
            val beat = t % 1.2f
            val u = if (beat < 0.15f || beat in 0.3f..0.45f) 5f else 4f
            fgAtt.bitmap(310f, 150f, u, HEART, C.HEART)
            for (i in 0 until 3) {
                if (sin(t * 1.5f + i * 2.1f) > -0.1f)
                    fgAbs.bitmap(listOf(28f, 340f, 60f)[i], listOf(130f, 250f, 300f)[i], 4f, SPARK, C.RAIN2)
            }
        }
    }

    // ---------- emocionado: salta sin parar, ojos de estrella ----------
    private fun excited(t: Float): SceneFrame {
        val hop = -abs(sin(t * 6.5f)) * 26f
        return scene(hop = hop, eyes = Eyes.STAR, mouth = Mouth.HAPPY) {
            for (i in 0 until 5) {
                val a = t * 2.5f + i * 2f * PI.toFloat() / 5f
                val x = 195f + 175f * kotlin.math.cos(a) - 12f
                val y = 230f + 120f * sin(a) - 12f
                val u = if (sin(t * 8f + i) > 0f) 5f else 3f
                fgAbs.bitmap(x, y, u, SPARK, C.GOLD)
            }
        }
    }

    // ---------- caricia: se pone feliz, corazones suben ----------
    private fun pet(t: Float): SceneFrame {
        val pattern = floatArrayOf(-6f, 0f, 6f, 0f)
        val dx = if (t < 1.2f) pattern[(t * 14f).toInt() % 4] else 0f
        return scene(bob = breath(t, 2.4f), dx = dx, eyes = Eyes.HAPPY, mouth = Mouth.SMALL, blush = true) {
            for (i in 0 until 3) {
                val ph = (t * 0.8f + i / 3f) % 1f
                val x = 110f + i * 75f + sin(t * 4f + i) * 10f
                fgAbs.bitmap(x, 150f - ph * 130f, 5f, HEART, withAlpha(C.HEART, (255 * (1f - ph * 0.8f)).toInt()))
            }
        }
    }

    // ---------- lluvia: la lluvia cae; saca el paraguas UNA vez y se queda con él ----------
    private fun rain(t: Float): SceneFrame {
        val poleBotX = 330f
        val poleBotY = 478f
        val topX = 240f
        val topY = 118f
        val grow = stepped(ramp(t, 0.15f, 0.5f), 6)
        val open = stepped(ramp(t, 0.7f, 0.8f), 5)
        val curTop = poleBotY + (topY - poleBotY) * grow
        val rx = 14f + 151f * open
        val ry = 8f + 54f * open
        return scene(bob = breath(t, 1.2f), eyes = blink(t), gy = if (t < 1.8f) -10f else 0f) {
            bgAbs.ellipse(195f, 556f, 150f, 14f, C.PUDDLE)
            if (grow > 0f) {
                var y = curTop
                while (y < poleBotY) {
                    val k = (poleBotY - y) / (poleBotY - topY)
                    bgAbs.rect(poleBotX + (topX - poleBotX) * k - 3f, y, G, G, C.POLE)
                    y += G
                }
            }
            if (open > 0f) fgAbs.dome(topX, topY, rx, ry, C.UMB1, C.UMB2, 6)
            for (i in 0 until 16) {
                val x = ((i * 53 + 17) % 372).toFloat() + 6f
                val speed = 520f + (i % 3) * 70f
                val y = (t * speed + i * 131f) % 600f - 20f
                val under = open > 0f && abs(x - topX) < rx
                if (under) {
                    val k = (x - topX) / rx
                    val surface = topY - ry * sqrt(max(0f, 1f - k * k))
                    if (y + 18f < surface) fgAbs.rect(x, y, 4f, 18f, C.RAIN, snap = false)
                    else if (y < surface + 12f) {
                        fgAbs.rect(x - 8f, surface - 6f, 4f, 4f, C.RAIN2, snap = false)
                        fgAbs.rect(x + 6f, surface - 6f, 4f, 4f, C.RAIN2, snap = false)
                    }
                } else if (y < 548f) fgAbs.rect(x, y, 4f, 18f, C.RAIN, snap = false)
                else if (y < 566f) fgAbs.rect(x - 9f, 552f, 18f, 4f, C.RAIN2, snap = false)
            }
        }
    }

    // ---------- calor: suda; saca el abanico UNA vez y se abanica ----------
    private fun hot(t: Float): SceneFrame {
        val fan = t >= 1.6f
        val open = stepped(ramp(t, 1.6f, 0.7f), 5)
        val bob = if (!fan) (if (sin(t * 7f) > 0f) G else 0f) else breath(t, 2.2f)
        val eyes = if (!fan) Eyes.TIRED else if (open >= 0.6f) Eyes.SMILE else Eyes.TIRED
        val mouth = if (!fan && (t * 3f).toInt() % 2 == 0) Mouth.YAWN_S else Mouth.NONE
        return scene(bob = bob, eyes = eyes, mouth = mouth) {
            if (!fan) {
                for (i in 0 until 3) {
                    val x = 150f + i * 45f
                    val y = 100f - (t * 40f + i * 30f) % 60f
                    val sway = if (((t * 4f).toInt() + i) % 2 == 0) 0f else G
                    fgAbs.rect(x + sway, y, G, 12f, withAlpha(C.HEAT, 200), snap = false)
                }
            }
            val drops = if (fan) 1 else 3
            for (i in 0 until drops) {
                val ph = (t * 0.6f + i / 3f) % 1f
                fgAbs.bitmap(14f, 190f + ph * 140f, 4f, DROP, C.SWEAT)
                fgAbs.bitmap(354f, 210f + ((ph + 0.4f) % 1f) * 140f, 4f, DROP, C.SWEAT)
            }
            if (fan) {
                // mango desde la mano hasta la base del abanico
                var y = 394f
                while (y < 470f) {
                    val k = (y - 394f) / 76f
                    bgAbs.rect(332f - 14f * k, y, G, G, C.POLE)
                    y += G
                }
                val rows = 1 + (open * 9f).toInt()
                val wave = if ((t * 4f).toInt() % 2 == 0) -1f else 1f
                for (r in 0 until rows) {
                    var half = (r + 1) * G
                    if (r >= 8) half -= (r - 7) * G * 0.7f
                    val xc = 336f - r * G * 0.25f + wave * G * 1.5f * (r / 9f)
                    val yy = 392f - r * G
                    var x = xc - half
                    var idx = 0
                    while (x < xc + half) {
                        val x1 = minOf(x + 2f * G, xc + half)
                        val edge = x + 2f * G >= xc + half || idx == 0
                        fgAtt.span(x, x1, yy, if (edge || r == rows - 1) C.FANR else if (idx % 2 == 0) C.FAN1 else C.FAN2)
                        x += 2f * G
                        idx++
                    }
                }
            }
        }
    }

    // ---------- frío: tiembla, se pone la bufanda UNA vez y se frota los brazos con guantes ----------
    private fun cold(t: Float): SceneFrame {
        val shiverNow = t < 2.4f || (t % 7f) < 0.6f
        val pattern = floatArrayOf(-6f, 0f, 6f, 0f)
        val dx = if (shiverNow) pattern[(t * 16f).toInt() % 4] else 0f
        val scarfR = stepped(ramp(t, 1.3f, 1.0f), 6)
        val hug = t >= 2.5f
        val eyes = if (scarfR >= 1f && !shiverNow) Eyes.SMILE else Eyes.SQUINT
        return scene(dx = dx, eyes = eyes, mouth = if (hug) Mouth.SMALL else Mouth.NONE) {
            // nieve
            for (i in 0 until 12) {
                val x = ((i * 61 + 23) % 372).toFloat()
                val y = (t * 70f + i * 53f) % 640f
                fgAbs.bitmap(x, y, 3f, FLAKE, C.SNOW)
            }
            if (hug) {
                // se tapan los brazos de fábrica y se dibujan cruzados sobre el pecho
                eraseAtt.rect(30f, 392f, 81f, 80f, C.BG, snap = false)
                eraseAtt.rect(281f, 392f, 81f, 80f, C.BG, snap = false)
                // dos antebrazos en diagonal, cruzados sobre el pecho (el segundo es el espejo del primero)
                val m = 34f / 174f
                for (mirror in listOf(false, true)) {
                    for (pad in listOf(6f, 0f)) {
                        var yy = 408f - pad
                        while (yy < 440f + pad) {
                            val yc = yy + G / 2f
                            val lo = maxOf(108f - pad, 108f + (yc - pad - 438f) / m)
                            val hi = minOf(282f + pad, 108f + (yc + pad - 408f) / m)
                            if (hi > lo) {
                                val c = if (pad > 0f) C.OUTLINE else C.SLEEVE
                                if (mirror) fgAtt.span(390f - hi, 390f - lo, yy, c) else fgAtt.span(lo, hi, yy, c)
                            }
                            yy += G
                        }
                    }
                }
                val up = ((t * 3f).toInt() % 2) == 0
                val yr = if (up) 426f else 436f
                val yl = if (up) 436f else 426f
                fgAtt.rect(264f, yr, 42f, 30f, C.MITT)
                fgAtt.rect(264f, yr, 6f, 30f, C.CREAM)
                fgAtt.rect(84f, yl, 42f, 30f, C.MITT)
                fgAtt.rect(120f, yl, 6f, 30f, C.CREAM)
            }
            if (scarfR > 0f) {
                val x1 = 102f + 192f * scarfR
                for (i in 0 until 5) {
                    fgAtt.span(102f, x1, 342f + i * G, if (i == 2) C.SCARF2 else C.SCARF)
                }
                if (scarfR >= 1f) {
                    val sway = if (sin(t * 2.4f) > 0f) G else 0f
                    for (i in 0 until 11) {
                        fgAtt.span(252f + sway, 282f + sway, 372f + i * G, if (i % 4 == 2) C.SCARF2 else C.SCARF)
                    }
                }
            }
        }
    }
}
