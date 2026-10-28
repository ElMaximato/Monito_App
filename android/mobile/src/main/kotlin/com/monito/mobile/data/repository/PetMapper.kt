package com.monito.mobile.data.repository

import com.monito.mobile.data.remote.dto.DailySummaryDto
import com.monito.mobile.data.remote.dto.PetDto
import com.monito.mobile.data.remote.dto.StateMinutesDto
import com.monito.mobile.domain.model.DailySummary
import com.monito.mobile.domain.model.HeartRateStats
import com.monito.mobile.domain.model.Pet
import com.monito.mobile.domain.model.StateMinutes
import com.monito.shared.state.PetState

fun PetDto.toDomain() = Pet(
    name = name,
    personality = personality,
    level = level,
    xp = xp,
    currentLevelXp = currentLevelXp,
    nextLevelXp = nextLevelXp,
    petCount = stats.petCount,
    shakeCount = stats.shakeCount,
)

fun StateMinutesDto.toDomain() = StateMinutes(PetState.fromCode(state), minutes)

fun DailySummaryDto.toDomain() = DailySummary(
    trackedMinutes = trackedMinutes,
    movingMinutes = movingMinutes,
    restingMinutes = restingMinutes,
    sleepMinutes = sleepMinutes,
    steps = steps,
    heartRate = heartRate?.let { HeartRateStats(it.avg, it.min, it.max, it.samples) },
)
