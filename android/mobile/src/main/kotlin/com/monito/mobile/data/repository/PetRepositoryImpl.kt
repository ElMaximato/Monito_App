package com.monito.mobile.data.repository

import com.monito.mobile.config.AppConfig
import com.monito.mobile.data.remote.api.MonitoApi
import com.monito.mobile.data.remote.dto.EventRequest
import com.monito.mobile.data.remote.dto.RegisterPetRequest
import com.monito.mobile.data.remote.dto.SampleRequest
import com.monito.mobile.data.remote.dto.StateLogRequest
import com.monito.mobile.data.remote.dto.UpdatePetRequest
import com.monito.mobile.domain.model.Pet
import com.monito.mobile.domain.model.StateMinutes
import com.monito.mobile.domain.repository.PetRepository
import com.monito.shared.protocol.SampleMessage
import com.monito.shared.protocol.StateMessage

class PetRepositoryImpl(
    private val api: MonitoApi,
    private val device: DevicePreferences,
) : PetRepository {

    private val deviceId get() = device.deviceId

    override suspend fun ensurePet(): Pet = api.registerPet(
        RegisterPetRequest(deviceId, AppConfig.DEFAULT_PET_NAME, "curioso"),
    ).toDomain()

    override suspend fun getPet(): Pet = api.getPet(deviceId).toDomain()

    override suspend fun updatePet(name: String?, personality: String?): Pet =
        api.updatePet(deviceId, UpdatePetRequest(name, personality)).toDomain()

    override suspend fun registerEvent(type: String, at: Long): Pet =
        api.sendEvent(deviceId, EventRequest(type, at)).toDomain()

    override suspend fun logState(message: StateMessage) {
        api.logState(deviceId, StateLogRequest(message.state.name, message.startedAt))
    }

    override suspend fun sendSample(message: SampleMessage) {
        api.sendSample(
            deviceId,
            SampleRequest(
                recordedAt = message.recordedAt,
                temperatureC = message.temperatureC,
                heartRate = message.heartRate,
                steps = message.steps,
                pressureHpa = message.pressureHpa,
            ),
        )
    }

    override suspend fun getStateMinutes(): List<StateMinutes> =
        api.getStateMinutes(deviceId).map { it.toDomain() }
}
