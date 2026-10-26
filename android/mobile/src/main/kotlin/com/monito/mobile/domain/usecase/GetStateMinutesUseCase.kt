package com.monito.mobile.domain.usecase

import com.monito.mobile.domain.model.StateMinutes
import com.monito.mobile.domain.repository.PetRepository

class GetStateMinutesUseCase(private val repository: PetRepository) {
    suspend operator fun invoke(): List<StateMinutes> = repository.getStateMinutes()
}
