package com.monito.mobile.domain.usecase

import com.monito.mobile.domain.model.DailySummary
import com.monito.mobile.domain.repository.PetRepository

class GetDailySummaryUseCase(private val repository: PetRepository) {
    /** fromMillis y toMillis delimitan el día local (epoch ms). */
    suspend operator fun invoke(fromMillis: Long, toMillis: Long): DailySummary =
        repository.getDailySummary(fromMillis, toMillis)
}
