package com.pawsmedic.desktop.veterinary.model

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
    @SerialName("status") val status: String = "",
    @SerialName("tenant_id") val tenantId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("decision_reason") val decisionReason: String? = null
)

@Serializable
data class TenantDto(
    @SerialName("id") val id: String? = null,
    @SerialName("status") val status: String = ""
)

suspend fun veterinaryAccessStatus(
    supabase: io.github.jan.supabase.SupabaseClient,
    userId: String
): BusinessApplicationDto? {
    val applications = supabase.postgrest["business_applications"]
        .select { filter { eq("applicant_id", userId) } }
        .decodeList<BusinessApplicationDto>()
    val businesses = supabase.postgrest["businesses"]
        .select { filter { eq("submitted_by", userId) } }
        .decodeList<BusinessApplicationDto>()
    return selectVeterinaryAccessStatus(applications, businesses)
}

internal fun selectVeterinaryAccessStatus(
    applications: List<BusinessApplicationDto>,
    businesses: List<BusinessApplicationDto>
): BusinessApplicationDto? {
    val latestApplication = applications.maxByOrNull { it.createdAt.orEmpty() }
    val latestBusiness = businesses.maxByOrNull { it.createdAt.orEmpty() }
    val latest = listOfNotNull(latestApplication, latestBusiness)
    return latest.firstOrNull { it.status == "PENDING" }
        ?: latest.firstOrNull { it.status == "REJECTED" || it.status == "SUSPENDED" }
        ?: latest.firstOrNull { it.status != "APPROVED" }
        ?: latest.firstOrNull()
}

suspend fun resolveTenantId(
    supabase: io.github.jan.supabase.SupabaseClient,
    userId: String,
    profile: com.pawsmedic.shared.core.model.UserProfile
): String {
    val existing = profile.tenantId
    if (!existing.isNullOrBlank()) {
        return existing
    }

    val business = supabase.postgrest["businesses"]
        .select { filter { eq("submitted_by", userId); eq("status", "APPROVED") } }
        .decodeList<BusinessApplicationDto>().maxByOrNull { it.createdAt.orEmpty() }
        ?: error("No existe una solicitud aprobada para esta cuenta.")
    val tenantId = business.tenantId?.takeIf { it.isNotBlank() }
        ?: supabase.postgrest["tenants"]
            .select { filter { eq("owner_id", userId); eq("status", "ACTIVE") } }
            .decodeList<TenantDto>().singleOrNull()?.id
        ?: error("La solicitud aprobada no tiene una sucursal activa asignada.")
    supabase.postgrest.rpc("link_approved_tenant", buildJsonObject {
        put("p_tenant_id", tenantId)
    })
    return tenantId
}
