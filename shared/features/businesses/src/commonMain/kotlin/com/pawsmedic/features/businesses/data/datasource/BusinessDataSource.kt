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
    override suspend fun getBusinesses(): List<BusinessDto> = coroutineScope {
        val clinicsDeferred = async {
            supabase.postgrest["business_directory"]
                .select()
                .decodeList<BusinessDirectoryRecord>()
        }
        val servicesDeferred = async {
            supabase.postgrest["services"]
                .select {
                    filter { eq("active", true) }
                }
                .decodeList<BusinessServiceCategoryRecord>()
        }

        val categoriesByTenant = servicesDeferred.await()
            .groupBy { it.tenantId }
            .mapValues { (_, services) -> services.map { it.category }.distinct() }

        clinicsDeferred.await().map { clinic ->
            clinic.toDto(serviceCategories = categoriesByTenant[clinic.id].orEmpty())
        }
    }

    override suspend fun getBusinessById(id: String): BusinessDto? = coroutineScope {
        val businessDeferred = async {
            supabase.postgrest["business_directory"]
                .select {
                    filter { eq("id", id) }
                }
                .decodeList<BusinessDirectoryRecord>()
                .singleOrNull()
        }
        val schedulesDeferred = async { getSchedules(id) }
        val timeZoneDeferred = async { getTimeZone(id) }
        val servicesDeferred = async {
            supabase.postgrest["services"]
                .select {
                    filter { eq("tenant_id", id); eq("active", true) }
                }
                .decodeList<BusinessServiceCategoryRecord>()
        }

        val business = businessDeferred.await()
        business?.toDto(
            schedules = schedulesDeferred.await(),
            timeZone = timeZoneDeferred.await(),
            serviceCategories = servicesDeferred.await().map { it.category }.distinct()
        )
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
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null
) {
    fun toDto(
        schedules: List<BusinessScheduleDto> = emptyList(),
        timeZone: String = "America/Mexico_City",
        serviceCategories: List<String> = emptyList()
    ): BusinessDto {
        val resolvedLogo = logoUrl?.takeIf { it.isNotBlank() } ?: photoUrl
        return BusinessDto(
            id = id,
            name = name.ifBlank { publicName.ifBlank { legalName } },
            address = address,
            phone = phone,
            photoUrl = resolvedLogo,
            hours = schedules.map { schedule ->
                "${schedule.dayOfWeek.toWeekdayName()}: ${schedule.openTime.take(5)}–${schedule.closeTime.take(5)}"
            },
            schedules = schedules,
            serviceCategories = serviceCategories,
            timeZone = timeZone
        )
    }
}

@Serializable
private data class BusinessTimeZoneRecord(
    @SerialName("time_zone") val timeZone: String = "America/Mexico_City"
)

@Serializable
private data class BusinessServiceCategoryRecord(
    @SerialName("tenant_id") val tenantId: String,
    val category: String = "otros"
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
