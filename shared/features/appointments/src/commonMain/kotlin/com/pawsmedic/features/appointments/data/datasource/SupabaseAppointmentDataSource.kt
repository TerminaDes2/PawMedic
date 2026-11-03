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
        return supabase.postgrest.rpc(function = "get_owned_appointments")
            .decodeList<OwnedAppointmentRecord>()
            .map { row ->
                AppointmentDto(
                    id = row.id,
                    petId = row.petId,
                    petName = row.petName,
                    businessId = row.tenantId,
                    businessName = row.businessName,
                    serviceId = row.serviceId,
                    serviceName = row.serviceName,
                    date = row.localDate,
                    time = row.localTime,
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
            .map { AvailableAppointmentSlot(it.startsAt, it.endsAt, it.localTime) }

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
        ).decodeSingle<BookingResultRecord>()
        return appointment.copy(
            id = result.id,
            status = result.status.toDisplayStatus(),
            startsAt = result.startsAt,
            endsAt = result.endsAt
        )
    }
}

@Serializable
private data class OwnedAppointmentRecord(
    val id: String,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("pet_id") val petId: String,
    @SerialName("pet_name") val petName: String,
    @SerialName("business_name") val businessName: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("service_name") val serviceName: String,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    @SerialName("local_date") val localDate: String,
    @SerialName("local_time") val localTime: String,
    val status: String
)

@Serializable
private data class BookingResultRecord(
    val id: String,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    val status: String
)

@Serializable
private data class AvailableSlotRecord(
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    @SerialName("local_time") val localTime: String
)

@Serializable
private data class ServiceRecord(
    val id: String,
    val name: String,
    val description: String = "",
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("price_cents") val priceCents: Int = 0
)

private fun String.toDisplayStatus(): String = when (uppercase()) {
    "REQUESTED" -> "Pendiente de confirmación"
    "CONFIRMED" -> "Confirmada"
    "CANCELLED" -> "Cancelada"
    "COMPLETED" -> "Completada"
    "NO_SHOW" -> "No asistió"
    else -> this
}
