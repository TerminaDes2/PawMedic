package com.pawsmedic.features.services.data.datasource

import com.pawsmedic.features.services.data.dto.ServiceDto

interface ServiceDataSource {
    suspend fun listServices(businessId: String): List<ServiceDto>
}

class EmptyServiceDataSource : ServiceDataSource {
    override suspend fun listServices(businessId: String): List<ServiceDto> = emptyList()
}
