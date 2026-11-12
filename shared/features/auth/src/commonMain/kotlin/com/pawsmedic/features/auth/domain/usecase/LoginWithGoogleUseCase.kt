package com.pawsmedic.features.auth.domain.usecase

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.repository.AuthRepository

class LoginWithGoogleUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String, rawNonce: String? = null): AuthenticatedSession {
        require(idToken.isNotBlank()) { "No se pudo obtener el token de Google" }
        val session = repository.signInWithGoogleIdToken(idToken, rawNonce)
        return repository.requireCustomerSession(session)
    }
}
