package com.pawsmedic.features.platform_admin.domain.usecase

import com.pawsmedic.features.platform_admin.domain.model.BusinessApplication
import com.pawsmedic.features.platform_admin.domain.repository.PlatformAdminRepository

class ApproveBusinessUseCase(
    private val repository: PlatformAdminRepository
) {
    suspend operator fun invoke(applicationId: String): Result<BusinessApplication> =
        repository.approveBusiness(applicationId)
}
