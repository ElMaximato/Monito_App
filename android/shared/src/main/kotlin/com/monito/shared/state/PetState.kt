package com.monito.shared.state

/**
 * Estados del monito. Fuente única de verdad: reloj, móvil y la tabla `pet_state`
 * (database/seed.sql) deben coincidir en código y prioridad.
 *
 * priority: 1 gana sobre todos; a mayor número, menor prioridad.
 * Orden: caricia (interacción directa) > dormir (modo que elige el usuario) > susto > actividad
 * > pulso > clima > aburrido > normal. Dormir NO depende de la hora.
 * spriteName: nombre del PNG en drawable-nodpi, o null si el sprite aún no existe.
 */
enum class PetState(val priority: Int, val spriteName: String?, val displayName: String) {
    PET(1, "astro_excited", "Caricia"),
    NIGHT(2, "astro_sleep", "Dormir"),
    SCARED(3, "astro_scared", "Susto"),
    WALK(4, "astro_idle", "Caminando"),
    RUN(4, "astro_idle", "Corriendo"),
    CLIMB(4, null, "Trepando"),
    EXCITED(5, "astro_excited", "Emocionado"),
    CALM(5, "astro_sleep", "Tranquilo"),
    RAIN(6, "astro_rain", "Lluvia"),
    HOT(6, "astro_hot", "Calor"),
    COLD(6, "astro_cold", "Frío"),
    BORED(7, "astro_sit", "Aburrido"),
    IDLE(8, "astro_idle", "Normal");

    companion object {
        fun fromCode(code: String?): PetState = entries.firstOrNull { it.name == code } ?: IDLE
    }
}
