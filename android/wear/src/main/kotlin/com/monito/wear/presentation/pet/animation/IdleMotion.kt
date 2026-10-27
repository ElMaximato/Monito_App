package com.monito.wear.presentation.pet.animation

import kotlin.math.PI
import kotlin.math.sin

enum class EyeStyle { OPEN, CLOSED, HAPPY }

/**
 * Cómo se ve el monito en un instante del idle. Las medidas están en píxeles del escenario
 * (el mismo de los sprites: 390 x 629), no en píxeles de pantalla.
 */
data class IdleFrame(
    val bobY: Float,     // casco y torso bajan este tanto al respirar (los pies no se mueven)
    val hopY: Float,     // saltito: negativo = sube
    val glanceX: Float,  // hacia dónde mira
    val glanceY: Float,
    val eyes: EyeStyle,
)

/**
 * Lógica pura del idle "vivo" (sin Compose, se prueba en src/test). Misma idea que el hub del móvil:
 * el cuerpo es una imagen fija y lo que lo hace parecer vivo son ojos y movimientos hechos en código.
 */
object IdleMotion {
    /** Un "píxel" del arte, en píxeles del escenario. */
    const val STEP = 6f
    const val GLANCE = 12f

    private const val HOP_HEIGHT = 14f
    private const val HOP_SECONDS = 0.6f
    private const val HAPPY_EVERY_S = 14f   // cada cuánto se alegra solo
    private const val HAPPY_AT_S = 9f       // en qué segundo del ciclo ocurre
    private const val HAPPY_SECONDS = 1.4f  // cuánto dura contento

    fun frame(t: Float): IdleFrame {
        // Momento feliz: ojos ^ ^ y un saltito
        val happyT = t % HAPPY_EVERY_S - HAPPY_AT_S
        val happy = happyT in 0f..HAPPY_SECONDS
        val hopProgress = happyT / HOP_SECONDS
        val hop = if (happy && hopProgress <= 1f) -sin(hopProgress * PI.toFloat()) * HOP_HEIGHT else 0f

        // Respiración: el cuerpo baja un píxel y sube otro, cada ~2 segundos
        val bob = if (!happy && sin(t * 1.6f) > 0.15f) STEP else 0f

        // Mirada (ciclo de 10 s): centro -> izquierda -> arriba -> derecha -> centro
        val c = t % 10f
        val glanceX = when {
            c in 3f..4.6f -> -GLANCE
            c in 6.4f..8f -> GLANCE
            else -> 0f
        }
        val glanceY = if (c in 4.6f..5.2f) -6f else 0f

        // Parpadeo cada 4.6 s, y uno doble de vez en cuando
        val b = t % 4.6f
        val doubleBlink = (t / 4.6f).toInt() % 3 == 0 && b in 0.3f..0.43f
        val blinking = b < 0.13f || doubleBlink

        val eyes = when {
            happy -> EyeStyle.HAPPY
            blinking -> EyeStyle.CLOSED
            else -> EyeStyle.OPEN
        }
        return IdleFrame(
            bobY = bob,
            hopY = hop,
            glanceX = if (happy) 0f else glanceX,
            glanceY = if (happy) 0f else glanceY,
            eyes = eyes,
        )
    }
}
