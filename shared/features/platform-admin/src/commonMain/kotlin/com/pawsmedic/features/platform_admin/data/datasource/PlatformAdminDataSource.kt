package com.pawsmedic.features.platform_admin.data.datasource

import com.pawsmedic.features.platform_admin.data.dto.BusinessApplicationDto

interface PlatformAdminDataSource {
    suspend fun approveBusiness(applicationId: String): BusinessApplicationDto
}

class EmptyPlatformAdminDataSource : PlatformAdminDataSource {
    override suspend fun approveBusiness(applicationId: String): BusinessApplicationDto =
        throw UnsupportedOperationException("Platform admin data source is not configured")
}
