package com.monito.mobile.data.remote.dto

data class RegisterPetRequest(val deviceId: String, val name: String, val personality: String)

data class UpdatePetRequest(val name: String? = null, val personality: String? = null)

data class EventRequest(val type: String, val at: Long)

data class StateLogRequest(val state: String, val startedAt: Long)

data class SampleRequest(
    val recordedAt: Long,
    val temperatureC: Float? = null,
    val heartRate: Int? = null,
    val steps: Int? = null,
    val pressureHpa: Float? = null,
)
