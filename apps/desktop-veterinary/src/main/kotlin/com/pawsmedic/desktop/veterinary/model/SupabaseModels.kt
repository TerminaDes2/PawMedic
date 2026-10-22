package com.pawsmedic.desktop.veterinary.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val tenant_id: String? = null,
    val full_name: String? = null,
    val role: String = "veterinary"
)

@Serializable
data class BusinessApplication(
    val id: String? = null,
    val business_name: String,
    val tax_id: String,
    val applicant_name: String,
    val email: String,
    val phone: String,
    val address: String,
    val status: String = "pending"
)
