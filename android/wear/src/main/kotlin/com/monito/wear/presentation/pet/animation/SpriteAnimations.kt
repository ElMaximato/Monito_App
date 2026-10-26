package com.monito.wear.presentation.pet.animation

import com.monito.shared.state.PetState
import com.monito.wear.R

/**
 * GENERADO por tools/sprites/build_sheets.py a partir de tools/sprites/animations.json.
 * No editar a mano: cambia el JSON y vuelve a correr el script.
 */
object SpriteAnimations {
    val COLD = SpriteAnimation(
        sheetRes = R.drawable.anim_cold,
        frameWidth = 246, frameHeight = 318, columns = 8,
        introFrames = 32, loopFrames = 10, fps = 12.0f,
        stageX = -4.1f, stageY = 51.8f, stageWidth = 398.3f, stageHeight = 514.9f,
    )

    val SCARED = SpriteAnimation(
        sheetRes = R.drawable.anim_scared,
        frameWidth = 246, frameHeight = 274, columns = 8,
        introFrames = 13, loopFrames = 16, fps = 12.0f,
        stageX = -4.1f, stageY = 110.1f, stageWidth = 398.3f, stageHeight = 443.6f,
    )

    val HOT = SpriteAnimation(
        sheetRes = R.drawable.anim_hot,
        frameWidth = 206, frameHeight = 308, columns = 8,
        introFrames = 0, loopFrames = 24, fps = 10.0f,
        stageX = 13.7f, stageY = 59.9f, stageWidth = 333.5f, stageHeight = 498.7f,
    )

    val WALK = SpriteAnimation(
        sheetRes = R.drawable.anim_walk,
        frameWidth = 182, frameHeight = 296, columns = 8,
        introFrames = 0, loopFrames = 10, fps = 10.0f,
        stageX = 75.4f, stageY = 146.5f, stageWidth = 250.3f, stageHeight = 407.1f,
    )

    val RUN = SpriteAnimation(
        sheetRes = R.drawable.anim_walk,
        frameWidth = 182, frameHeight = 296, columns = 8,
        introFrames = 0, loopFrames = 10, fps = 16.0f,
        stageX = 75.4f, stageY = 146.5f, stageWidth = 250.3f, stageHeight = 407.1f,
    )

    /** Animación de un estado, o null si ese estado usa solo su imagen fija. */
    fun forState(state: PetState): SpriteAnimation? = when (state) {
        PetState.COLD -> COLD
        PetState.SCARED -> SCARED
        PetState.HOT -> HOT
        PetState.WALK -> WALK
        PetState.RUN -> RUN
        else -> null
    }
}
