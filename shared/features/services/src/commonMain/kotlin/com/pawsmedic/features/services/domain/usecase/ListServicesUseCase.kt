package com.pawsmedic.features.services.domain.usecase

import com.pawsmedic.features.services.domain.model.Service
import com.pawsmedic.features.services.domain.repository.ServiceRepository

class ListServicesUseCase(private val repository: ServiceRepository) {
    suspend operator fun invoke(businessId: String): Result<List<Service>> =
        repository.listServices(businessId)
}
