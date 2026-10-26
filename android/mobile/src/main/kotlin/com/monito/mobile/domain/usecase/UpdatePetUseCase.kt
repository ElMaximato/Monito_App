package com.monito.mobile.domain.usecase

import com.monito.mobile.domain.model.Pet
import com.monito.mobile.domain.repository.PetRepository

class UpdatePetUseCase(private val repository: PetRepository) {
    suspend operator fun invoke(name: String? = null, personality: String? = null): Pet =
        repository.updatePet(name, personality)
}
