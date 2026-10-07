package com.pawsmedic.desktop.veterinary.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
