package com.pawsmedic.features.appointments.data.datasource

import com.pawsmedic.features.appointments.data.dto.AppointmentDto
import com.pawsmedic.features.appointments.domain.model.AvailableAppointmentSlot
import com.pawsmedic.features.appointments.domain.model.VeterinaryService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAppointmentDataSource(
    private val supabase: SupabaseClient,
    private val ownerId: String
) : AppointmentDataSource {
    override suspend fun getAppointments(): List<AppointmentDto> {
        require(ownerId.isNotBlank()) { "No hay una cuenta autenticada para consultar las citas." }
        val rows = supabase.postgrest["appointments"]
            .select {
                filter { eq("booked_by", ownerId) }
            }
            .decodeList<AppointmentRecord>()

        return rows.map { row ->
            val pet = supabase.postgrest["pets"]
                .select {
                    filter { eq("id", row.petId) }
                }
                .decodeList<PetRecord>()
                .singleOrNull()
            val business = supabase.postgrest["business_directory"]
                .select {
                    filter { eq("id", row.tenantId) }
                }
                .decodeList<DirectoryRecord>()
                .singleOrNull()
            val service = supabase.postgrest["services"]
                .select {
                    filter { eq("id", row.serviceId) }
                }
                .decodeList<ServiceRecord>()
                .singleOrNull()

            AppointmentDto(
                id = row.id,
                petId = row.petId,
                petName = pet?.let { listOfNotNull(it.name, it.species).joinToString(" • ") }
                    ?: "Mascota (${row.petId})",
                businessId = row.tenantId,
                businessName = business?.name ?: "Clínica (${row.tenantId})",
                serviceId = row.serviceId,
                serviceName = service?.name ?: "Servicio (${row.serviceId})",
                date = row.startsAt.substringBefore('T'),
                time = row.startsAt.timeLabel(),
                notes = null,
                status = row.status.toDisplayStatus(),
                startsAt = row.startsAt,
                endsAt = row.endsAt
            )
        }
    }

    override suspend fun getServices(tenantId: String): List<VeterinaryService> =
        supabase.postgrest["services"]
            .select {
                filter {
                    eq("tenant_id", tenantId)
                    eq("active", true)
                }
            }
            .decodeList<ServiceRecord>()
            .map { service ->
                VeterinaryService(
                    id = service.id,
                    title = service.name,
                    description = service.description,
                    durationMinutes = service.durationMinutes,
                    priceCents = service.priceCents
                )
            }

    override suspend fun getAvailableSlots(
        tenantId: String,
        serviceId: String,
        date: String
    ): List<AvailableAppointmentSlot> =
        supabase.postgrest.rpc(
            function = "get_available_slots",
            parameters = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_service_id", serviceId)
                put("p_date", date)
            }
        ).decodeList<AvailableSlotRecord>()
            .map { AvailableAppointmentSlot(it.startsAt, it.endsAt, it.startsAt.timeLabel()) }

    override suspend fun requestAppointment(appointment: AppointmentDto): AppointmentDto {
        require(ownerId.isNotBlank()) { "No hay una cuenta autenticada para solicitar la cita." }
        val startsAt = requireNotNull(appointment.startsAt) {
            "Selecciona un horario válido antes de confirmar."
        }
        val endsAt = requireNotNull(appointment.endsAt) {
            "El horario seleccionado no incluye el final de la cita."
        }
        val idempotencyKey = requireNotNull(appointment.idempotencyKey) {
            "No se pudo preparar una clave segura para la solicitud. Inténtalo nuevamente."
        }
        val result = supabase.postgrest.rpc(
            function = "create_critical_booking",
            parameters = buildJsonObject {
                put("p_tenant_id", appointment.businessId)
                put("p_pet_id", appointment.petId)
                put("p_service_id", appointment.serviceId)
                put("p_booked_by", ownerId)
                put("p_starts_at", startsAt)
                put("p_ends_at", endsAt)
                put("p_idempotency_key", idempotencyKey)
            }
        ).decodeSingle<AppointmentRecord>()
        return appointment.copy(
            id = result.id,
            date = result.startsAt.substringBefore('T'),
            time = result.startsAt.timeLabel(),
            status = result.status.toDisplayStatus(),
            startsAt = result.startsAt,
            endsAt = result.endsAt
        )
    }
}

@Serializable
private data class AppointmentRecord(
    val id: String,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("pet_id") val petId: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    val status: String
)

@Serializable
private data class AvailableSlotRecord(
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String
)

@Serializable
private data class ServiceRecord(
    val id: String,
    val name: String,
    val description: String = "",
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("price_cents") val priceCents: Int = 0
)

@Serializable
private data class PetRecord(
    val name: String,
    val species: String
)

@Serializable
private data class DirectoryRecord(
    val name: String
)

private fun String.timeLabel(): String {
    val time = substringAfter('T', missingDelimiterValue = this)
    val offset = Regex("[+-]\\d{2}:\\d{2}$").find(time)?.value
    val zone = when {
        time.endsWith("Z") -> "UTC"
        offset == "+00:00" -> "UTC"
        offset != null -> offset
        else -> null
    }
    return listOfNotNull(time.take(5), zone).joinToString(" ")
}

private fun String.toDisplayStatus(): String = when (uppercase()) {
    "REQUESTED" -> "Pendiente de aprobación"
    "CONFIRMED" -> "Confirmada"
    "CANCELLED" -> "Cancelada"
    "COMPLETED" -> "Completada"
    "NO_SHOW" -> "No asistió"
    else -> this
}
