package com.pawsmedic.features.businesses.data.datasource

import com.pawsmedic.features.businesses.data.dto.BusinessDto
import com.pawsmedic.features.businesses.data.dto.BusinessScheduleDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface BusinessDataSource {
    suspend fun getBusinesses(): List<BusinessDto>
    suspend fun getBusinessById(id: String): BusinessDto?
}

class SupabaseBusinessDataSource(
    private val supabase: SupabaseClient
) : BusinessDataSource {
    override suspend fun getBusinesses(): List<BusinessDto> =
        supabase.postgrest["business_directory"]
            .select()
            .decodeList<BusinessDirectoryRecord>()
            .map { it.toDto(getSchedules(it.id), getTimeZone(it.id)) }

    override suspend fun getBusinessById(id: String): BusinessDto? {
        val business = supabase.postgrest["business_directory"]
            .select {
                filter { eq("id", id) }
            }
            .decodeList<BusinessDirectoryRecord>()
            .singleOrNull()
        return business?.toDto(getSchedules(id), getTimeZone(id))
    }

    private suspend fun getSchedules(tenantId: String): List<BusinessScheduleDto> =
        supabase.postgrest["business_schedules"]
            .select {
                filter {
                    eq("tenant_id", tenantId)
                    eq("is_active", true)
                }
            }
            .decodeList<BusinessScheduleDto>()
            .sortedWith(compareBy<BusinessScheduleDto> { it.dayOfWeek }.thenBy { it.openTime })

    private suspend fun getTimeZone(tenantId: String): String {
        val zones = supabase.postgrest["tenants"]
            .select {
                filter { eq("id", tenantId) }
            }
            .decodeList<BusinessTimeZoneRecord>()
        return zones.firstOrNull()?.timeZone?.takeIf { it.isNotBlank() }
            ?: "America/Mexico_City"
    }

}

@Serializable
private data class BusinessDirectoryRecord(
    val id: String,
    @SerialName("name") val name: String = "",
    @SerialName("public_name") val publicName: String = "",
    @SerialName("legal_name") val legalName: String = "",
    val address: String? = null,
    val phone: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null
) {
    fun toDto(schedules: List<com.pawsmedic.features.businesses.data.dto.BusinessScheduleDto>, timeZone: String) = BusinessDto(
        id = id,
        name = name.ifBlank { publicName.ifBlank { legalName } },
        address = address,
        phone = phone,
        photoUrl = photoUrl,
        hours = schedules.map { schedule ->
            "${schedule.dayOfWeek.toWeekdayName()}: ${schedule.openTime.take(5)}–${schedule.closeTime.take(5)}"
        },
        schedules = schedules,
        timeZone = timeZone
    )
}

@Serializable
private data class BusinessTimeZoneRecord(
    @SerialName("time_zone") val timeZone: String = "America/Mexico_City"
)

private fun Int.toWeekdayName() = when (this) {
    0 -> "Domingo"
    1 -> "Lunes"
    2 -> "Martes"
    3 -> "Miércoles"
    4 -> "Jueves"
    5 -> "Viernes"
    6 -> "Sábado"
    else -> "Día"
}
