package com.pawsmedic.features.platform_admin.domain.model

data class BusinessApplication(
    val id: String,
    val businessName: String,
    val applicantName: String,
    val submittedAt: String,
    val approved: Boolean = false
)
