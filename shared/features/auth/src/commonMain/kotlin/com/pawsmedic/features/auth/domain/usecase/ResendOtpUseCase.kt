package com.pawsmedic.features.auth.domain.usecase

import com.pawsmedic.features.auth.domain.repository.AuthRepository

class ResendOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String) {
        require(email.isNotBlank()) { "El correo electrónico es requerido" }
        repository.resendEmailOtp(email)
    }
}
