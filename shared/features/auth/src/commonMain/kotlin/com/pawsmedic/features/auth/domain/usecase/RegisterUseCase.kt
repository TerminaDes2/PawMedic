package com.pawsmedic.features.auth.domain.usecase

import com.pawsmedic.features.auth.domain.model.RegisterParams
import com.pawsmedic.features.auth.domain.repository.AuthRepository
import com.pawsmedic.features.auth.domain.repository.SignUpResult

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(params: RegisterParams): SignUpResult {
        require(params.firstName.isNotBlank()) { "El nombre es requerido" }
        require(params.lastName.isNotBlank()) { "El apellido es requerido" }
        require(params.email.isNotBlank()) { "El correo electrónico es requerido" }
        require(params.password.length >= 6) { "La contraseña debe tener al menos 6 caracteres" }
        return repository.signUp(params)
    }
}
