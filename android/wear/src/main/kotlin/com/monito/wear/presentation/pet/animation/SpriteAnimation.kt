package com.monito.wear.presentation.pet.animation

/**
 * Animación guardada como hoja de sprites: una sola imagen con todos los cuadros en cuadrícula,
 * de izquierda a derecha y de arriba abajo. Se reproducen primero los cuadros de introducción
 * (una vez) y luego los de bucle (para siempre).
 *
 * stageX/Y/Width/Height = dónde se dibuja un cuadro dentro del escenario de 390 × 629 unidades
 * (el mismo tamaño de los sprites fijos), ya escalado y alineado por las botas.
 * Los calcula tools/sprites/build_sheets.py.
 */
data class SpriteAnimation(
    val sheetRes: Int,
    val frameWidth: Int,
    val frameHeight: Int,
    val columns: Int,
    val introFrames: Int,
    val loopFrames: Int,
    val fps: Float,
    val stageX: Float,
    val stageY: Float,
    val stageWidth: Float,
    val stageHeight: Float,
) {
    init {
        require(loopFrames > 0) { "Una animación necesita al menos un cuadro de bucle" }
        require(columns > 0 && fps > 0f)
    }

    val totalFrames: Int get() = introFrames + loopFrames

    /** Índice del cuadro (0 .. totalFrames-1) que toca mostrar [elapsedNanos] después de empezar. */
    fun frameAt(elapsedNanos: Long): Int {
        val tick = (elapsedNanos.coerceAtLeast(0L) * fps.toDouble() / 1_000_000_000.0).toLong()
        return if (tick < introFrames) {
            tick.toInt()
        } else {
            introFrames + ((tick - introFrames) % loopFrames).toInt()
        }
    }

    fun column(frame: Int): Int = frame % columns
    fun row(frame: Int): Int = frame / columns
}
