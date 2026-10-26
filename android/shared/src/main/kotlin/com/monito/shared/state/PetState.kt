package com.monito.shared.state

/**
 * Estados del monito. Fuente única de verdad: reloj, móvil y la tabla `pet_state`
 * (database/seed.sql) deben coincidir en código y prioridad.
 *
 * priority: 1 gana sobre todos; a mayor número, menor prioridad.
 * spriteName: nombre del PNG en drawable-nodpi, o null si el sprite aún no existe.
 */
enum class PetState(val priority: Int, val spriteName: String?, val displayName: String) {
    SCARED(1, "astro_scared", "Susto"),
    PET(2, "astro_excited", "Caricia"),
    WALK(3, "astro_idle", "Caminando"),
    RUN(3, "astro_idle", "Corriendo"),
    CLIMB(3, null, "Trepando"),
    EXCITED(4, "astro_excited", "Emocionado"),
    CALM(4, "astro_sleep", "Tranquilo"),
    RAIN(5, "astro_rain", "Lluvia"),
    HOT(5, "astro_hot", "Calor"),
    COLD(5, "astro_cold", "Frío"),
    NIGHT(6, "astro_sleep", "Noche"),
    BORED(7, "astro_sit", "Aburrido"),
    IDLE(8, "astro_idle", "Normal");

    companion object {
        fun fromCode(code: String?): PetState = entries.firstOrNull { it.name == code } ?: IDLE
    }
}
