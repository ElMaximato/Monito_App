package com.monito.mobile.domain.model

data class Pet(
    val name: String,
    val personality: String,
    val level: Int,
    val xp: Int,
    val currentLevelXp: Int,
    val nextLevelXp: Int,
    val petCount: Int,
    val shakeCount: Int,
) {
    /** Avance dentro del nivel actual, de 0f a 1f. */
    val levelProgress: Float
        get() {
            val span = (nextLevelXp - currentLevelXp).coerceAtLeast(1)
            return ((xp - currentLevelXp).toFloat() / span).coerceIn(0f, 1f)
        }
}
