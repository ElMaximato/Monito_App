package com.monito.shared.protocol

/** Rutas de la Wearable Data Layer. Ver docs/ARQUITECTURA.md. */
object DataLayerPaths {
    // reloj -> móvil
    const val EVENT_SHAKE = "/event/shake"
    const val EVENT_PET = "/event/pet"
    const val SAMPLE = "/sample"
    const val STATE = "/state"

    // móvil -> reloj
    const val WEATHER = "/weather"
    const val PROFILE = "/profile"
}
