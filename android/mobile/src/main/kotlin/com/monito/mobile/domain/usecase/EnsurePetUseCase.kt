package com.monito.mobile.domain.usecase

import com.monito.mobile.domain.model.Pet
import com.monito.mobile.domain.repository.PetRepository

class EnsurePetUseCase(private val repository: PetRepository) {
    suspend operator fun invoke(): Pet = repository.ensurePet()
}
