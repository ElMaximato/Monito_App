package com.monito.mobile.data.remote.api

import com.monito.mobile.data.remote.dto.EventRequest
import com.monito.mobile.data.remote.dto.PetDto
import com.monito.mobile.data.remote.dto.RegisterPetRequest
import com.monito.mobile.data.remote.dto.SampleRequest
import com.monito.mobile.data.remote.dto.StateLogRequest
import com.monito.mobile.data.remote.dto.StateMinutesDto
import com.monito.mobile.data.remote.dto.UpdatePetRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Cliente de nuestro backend (ver docs/API.md). */
interface MonitoApi {
    @POST("api/pets")
    suspend fun registerPet(@Body body: RegisterPetRequest): PetDto

    @GET("api/pets/{deviceId}")
    suspend fun getPet(@Path("deviceId") deviceId: String): PetDto

    @PATCH("api/pets/{deviceId}")
    suspend fun updatePet(@Path("deviceId") deviceId: String, @Body body: UpdatePetRequest): PetDto

    @POST("api/pets/{deviceId}/events")
    suspend fun sendEvent(@Path("deviceId") deviceId: String, @Body body: EventRequest): PetDto

    @POST("api/pets/{deviceId}/states")
    suspend fun logState(@Path("deviceId") deviceId: String, @Body body: StateLogRequest)

    @POST("api/pets/{deviceId}/samples")
    suspend fun sendSample(@Path("deviceId") deviceId: String, @Body body: SampleRequest)

    @GET("api/pets/{deviceId}/state-minutes")
    suspend fun getStateMinutes(@Path("deviceId") deviceId: String): List<StateMinutesDto>
}
