package com.pawsmedic.features.appointments.domain.usecase

import com.pawsmedic.features.appointments.domain.model.Appointment
import com.pawsmedic.features.appointments.domain.repository.AppointmentRepository

class RequestAppointmentUseCase(private val repository: AppointmentRepository) {
    suspend operator fun invoke(appointment: Appointment): Result<Appointment> =
        repository.requestAppointment(appointment)
}
