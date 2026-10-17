package com.pawsmedic.features.auth.domain.usecase

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.repository.AuthRepository

class VerifyOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, code: String): AuthenticatedSession {
        require(email.isNotBlank()) { "El correo electrónico es requerido" }
        require(code.trim().length >= 6) { "Ingresa el código completo de 6 dígitos" }
        val session = repository.verifyEmailOtp(email, code)
        return repository.requireCustomerSession(session)
    }
}
