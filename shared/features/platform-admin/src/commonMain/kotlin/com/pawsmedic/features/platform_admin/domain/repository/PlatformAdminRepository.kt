package com.pawsmedic.features.platform_admin.domain.repository

import com.pawsmedic.features.platform_admin.domain.model.BusinessApplication

interface PlatformAdminRepository {
    suspend fun approveBusiness(applicationId: String): Result<BusinessApplication>
}
