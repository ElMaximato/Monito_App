package com.monito.mobile.data.remote.dto

data class HeartRateStatsDto(val avg: Int, val min: Int, val max: Int, val samples: Int)

/** Respuesta de GET /pets/:deviceId/summary (ver docs/API.md). */
data class DailySummaryDto(
    val trackedMinutes: Double,
    val movingMinutes: Double,
    val restingMinutes: Double,
    val sleepMinutes: Double,
    val steps: Int,
    val heartRate: HeartRateStatsDto?,
)
