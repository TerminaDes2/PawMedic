package com.pawsmedic.features.auth.domain.usecase

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.model.LoginParams
import com.pawsmedic.features.auth.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(params: LoginParams): AuthenticatedSession {
        require(params.email.isNotBlank()) { "El correo electrónico es requerido" }
        require(params.password.isNotBlank()) { "La contraseña es requerida" }
        return repository.signIn(params)
    }
}
