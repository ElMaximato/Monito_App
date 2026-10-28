package com.monito.mobile.domain.repository

import com.monito.mobile.domain.model.DailySummary
import com.monito.mobile.domain.model.Pet
import com.monito.mobile.domain.model.StateMinutes
import com.monito.shared.protocol.SampleMessage
import com.monito.shared.protocol.StateMessage

interface PetRepository {
    /** Crea el monito de este teléfono si no existe y lo devuelve. */
    suspend fun ensurePet(): Pet
    suspend fun getPet(): Pet
    /** Cambia el nombre y/o la personalidad; lo que sea null no se toca. */
    suspend fun updatePet(name: String?, personality: String?): Pet
    suspend fun registerEvent(type: String, at: Long): Pet
    suspend fun logState(message: StateMessage)
    suspend fun sendSample(message: SampleMessage)
    suspend fun getStateMinutes(): List<StateMinutes>
    /** Resumen del tramo [fromMillis, toMillis] (el día local completo). */
    suspend fun getDailySummary(fromMillis: Long, toMillis: Long): DailySummary
}
