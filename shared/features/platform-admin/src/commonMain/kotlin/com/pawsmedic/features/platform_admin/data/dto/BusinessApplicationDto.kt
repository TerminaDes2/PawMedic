package com.pawsmedic.features.platform_admin.data.dto

import com.pawsmedic.features.platform_admin.domain.model.BusinessApplication
import kotlinx.serialization.Serializable

@Serializable
data class BusinessApplicationDto(
    val id: String,
    val businessName: String,
    val applicantName: String,
    val submittedAt: String,
    val approved: Boolean = false
) {
    fun toDomain() = BusinessApplication(
        id,
        businessName,
        applicantName,
        submittedAt,
        approved
    )
}
