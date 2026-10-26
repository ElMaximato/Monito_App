package com.monito.mobile.data.remote.dto

data class PetStatsDto(val petCount: Int, val shakeCount: Int)

data class PetDto(
    val id: Int,
    val deviceId: String,
    val name: String,
    val personality: String,
    val level: Int,
    val xp: Int,
    val currentLevelXp: Int,
    val nextLevelXp: Int,
    val stats: PetStatsDto,
)

data class StateMinutesDto(val state: String, val minutes: Double)
