package com.pawsmedic.features.businesses.data.datasource

import com.pawsmedic.features.businesses.data.dto.BusinessDto
import com.pawsmedic.features.businesses.data.dto.BusinessScheduleDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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
            .map { it.toDto() }

    override suspend fun getBusinessById(id: String): BusinessDto? = coroutineScope {
        val business = async {
            supabase.postgrest["business_directory"]
                .select {
                    filter { eq("id", id) }
                }
                .decodeList<BusinessDirectoryRecord>()
                .singleOrNull()
        }
        val schedules = async { getSchedules(id) }
        business.await()?.toDto(schedules.await())
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
            .sortedWith(compareBy(BusinessScheduleDto::dayOfWeek, BusinessScheduleDto::openTime))
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
    fun toDto(schedules: List<BusinessScheduleDto> = emptyList()) = BusinessDto(
        id = id,
        name = name.ifBlank { publicName.ifBlank { legalName } },
        address = address,
        phone = phone,
        hours = schedules.map { "${it.dayOfWeek.toWeekdayName()}: ${it.openTime.take(5)}–${it.closeTime.take(5)}" },
        schedules = schedules
    )
}

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
