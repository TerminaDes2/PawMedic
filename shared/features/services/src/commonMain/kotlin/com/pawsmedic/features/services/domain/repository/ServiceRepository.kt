package com.pawsmedic.features.services.domain.repository

import com.pawsmedic.features.services.domain.model.Service

interface ServiceRepository {
    suspend fun listServices(businessId: String): Result<List<Service>>
}
