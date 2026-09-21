package com.pawsmedic.features.auth.domain.usecase

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.repository.AuthRepository

class RestoreSessionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AuthenticatedSession? = repository.restoreSession()
}
