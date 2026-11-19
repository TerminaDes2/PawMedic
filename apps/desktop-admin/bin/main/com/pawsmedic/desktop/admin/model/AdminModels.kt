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
    @SerialName("id") val id: String,
    @SerialName("submitted_by") val submittedBy: String? = null,
    @SerialName("applicant_id") val applicantId: String? = null,
    @SerialName("tenant_id") val tenantId: String? = null,
    @SerialName("applicant_name") val applicantNameRaw: String = "",
    @SerialName("veterinarian_name") val veterinarianNameRaw: String = "",
    @SerialName("legal_name") val legalNameRaw: String = "",
    @SerialName("public_name") val publicNameRaw: String = "",
    @SerialName("business_name") val businessNameRaw: String = "",
    @SerialName("tax_id") val taxId: String = "",
    @SerialName("email") val emailRaw: String = "",
    @SerialName("contact_email") val contactEmailRaw: String = "",
    @SerialName("phone") val phoneRaw: String = "",
    @SerialName("contact_phone") val contactPhoneRaw: String = "",
    @SerialName("address") val address: String = "",
    @SerialName("license_number") val licenseNumberRaw: String = "",
    @SerialName("professional_license") val professionalLicenseRaw: String = "",
    @SerialName("status") val status: String = "PENDING",
    @SerialName("decision_reason") val decisionReason: String? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    val applicantName: String get() = veterinarianNameRaw.ifBlank { applicantNameRaw }
    val publicName: String get() = businessNameRaw.ifBlank { publicNameRaw.ifBlank { legalNameRaw } }
    val email: String get() = contactEmailRaw.ifBlank { emailRaw }
    val phone: String get() = contactPhoneRaw.ifBlank { phoneRaw }
    val licenseNumber: String get() = professionalLicenseRaw.ifBlank { licenseNumberRaw }
}
