package com.pawsmedic.features.appointments.data.datasource

import com.pawsmedic.features.appointments.data.dto.AppointmentDto

class InMemoryAppointmentDataSource : AppointmentDataSource {
    private val appointments = mutableListOf(
        AppointmentDto(
            id = "app-1",
            petId = "pet-1",
            petName = "Tobías (Gato)",
            businessId = "biz-1",
            businessName = "Clinipet Central",
            serviceId = "srv-1",
            serviceName = "Consulta Médica General",
            date = "Domingo, 15 de Nov",
            time = "10:00 AM",
            notes = "Revisión anual de rutina",
            status = "Pendiente de aprobación"
        )
    )

    override suspend fun getAppointments(): List<AppointmentDto> {
        return appointments.filter { it.status.contains("Pendiente", ignoreCase = true) || it.status.equals("REQUESTED", ignoreCase = true) }
    }

    override suspend fun requestAppointment(appointment: AppointmentDto): AppointmentDto {
        val newApp = if (appointment.id.isBlank()) appointment.copy(id = "app-${appointments.size + 1}") else appointment
        appointments.add(0, newApp)
        return newApp
    }
}
