package com.pawsmedic.features.appointments.data.datasource

import com.pawsmedic.features.appointments.data.dto.AppointmentDto

/**
 * Implementación de la fuente de datos de Citas para Supabase.
 */
class SupabaseAppointmentDataSource : AppointmentDataSource {

    override suspend fun getAppointments(): List<AppointmentDto> {
        return emptyList()
    }

    override suspend fun requestAppointment(appointment: AppointmentDto): AppointmentDto {
        val newId = "app-${(1000..9999).random()}"
        return appointment.copy(id = newId)
    }
}
