package com.pawsmedic.desktop.admin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AccountType {
    VETERINARIA,
    SUPERADMIN
}

@Serializable
data class AccountApplicationDto(
    @SerialName("id") val id: String? = null,
    @SerialName("type") val type: AccountType = AccountType.VETERINARIA,
    @SerialName("applicant_name") val applicantName: String = "",
    @SerialName("business_name") val businessName: String = "",
    @SerialName("tax_id") val taxId: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("phone") val phone: String = "",
    @SerialName("address") val address: String = "",
    @SerialName("license_number") val licenseNumber: String = "",
    @SerialName("status") val status: String = "PENDIENTE",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class BusinessApplicationDto(
    @SerialName("id") val id: String? = null,
    @SerialName("business_name") val businessName: String = "",
    @SerialName("tax_id") val taxId: String = "",
    @SerialName("applicant_name") val applicantName: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("phone") val phone: String = "",
    @SerialName("address") val address: String = "",
    @SerialName("status") val status: String = "PENDIENTE"
)
