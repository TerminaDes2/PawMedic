package com.pawsmedic.features.platform_admin.data.repository

import com.pawsmedic.features.platform_admin.data.datasource.PlatformAdminDataSource
import com.pawsmedic.features.platform_admin.data.dto.BusinessApplicationDto
import com.pawsmedic.features.platform_admin.domain.model.BusinessApplication
import com.pawsmedic.features.platform_admin.domain.repository.PlatformAdminRepository

class DefaultPlatformAdminRepository(
    private val dataSource: PlatformAdminDataSource
) : PlatformAdminRepository {
    override suspend fun approveBusiness(
        applicationId: String
    ): Result<BusinessApplication> = try {
        Result.success(
            dataSource.approveBusiness(applicationId).toDomain()
        )
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
