package com.pawsmedic.features.businesses.data.datasource

import com.pawsmedic.features.businesses.data.dto.BusinessDto
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
            .map { it.toDto(getHours(it.id)) }

    override suspend fun getBusinessById(id: String): BusinessDto? {
        val business = supabase.postgrest["business_directory"]
            .select {
                filter { eq("id", id) }
            }
            .decodeList<BusinessDirectoryRecord>()
            .singleOrNull()
        return business?.toDto(getHours(id))
    }

    private suspend fun getHours(tenantId: String): List<String> =
        supabase.postgrest["business_schedules"]
            .select {
                filter { eq("tenant_id", tenantId) }
            }
            .decodeList<BusinessScheduleRecord>()
            .sortedBy(BusinessScheduleRecord::dayOfWeek)
            .map { schedule ->
                "${schedule.dayOfWeek.toWeekdayName()}: ${schedule.openTime.take(5)}–${schedule.closeTime.take(5)}"
            }
}

@Serializable
private data class BusinessDirectoryRecord(
    val id: String,
    @SerialName("name") val name: String = "",
    @SerialName("public_name") val publicName: String = "",
    @SerialName("legal_name") val legalName: String = "",
    val address: String? = null,
    val phone: String? = null
) {
    fun toDto(hours: List<String>) = BusinessDto(
        id = id,
        name = name.ifBlank { publicName.ifBlank { legalName } },
        address = address,
        phone = phone,
        hours = hours
    )
}

@Serializable
private data class BusinessScheduleRecord(
    @SerialName("day_of_week") val dayOfWeek: Int,
    @SerialName("open_time") val openTime: String,
    @SerialName("close_time") val closeTime: String
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
