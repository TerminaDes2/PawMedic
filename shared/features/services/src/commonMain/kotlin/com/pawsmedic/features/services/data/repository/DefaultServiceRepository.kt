package com.pawsmedic.features.services.data.repository

import com.pawsmedic.features.services.data.datasource.ServiceDataSource
import com.pawsmedic.features.services.domain.model.Service
import com.pawsmedic.features.services.domain.repository.ServiceRepository

class DefaultServiceRepository(
    private val dataSource: ServiceDataSource
) : ServiceRepository {
    override suspend fun listServices(businessId: String): Result<List<Service>> = try {
        Result.success(dataSource.listServices(businessId).map { it.toDomain() })
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
