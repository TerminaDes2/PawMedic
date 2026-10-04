package com.pawsmedic.features.auth.domain.repository

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.model.PawMedicRole

interface AuthRepository {
    suspend fun restoreSession(): AuthenticatedSession?

    suspend fun signIn(
        email: String,
        password: String,
        requiredRole: PawMedicRole? = null
    ): AuthenticatedSession

    suspend fun signUp(
        email: String,
        password: String,
        role: PawMedicRole,
        fullName: String? = null
    ): AuthenticatedSession

    suspend fun signOut()
}