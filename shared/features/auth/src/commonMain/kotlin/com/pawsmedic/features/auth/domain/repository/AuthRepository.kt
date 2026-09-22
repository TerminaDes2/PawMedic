package com.pawsmedic.features.auth.domain.repository

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession

interface AuthRepository {
    suspend fun restoreSession(): AuthenticatedSession?
    suspend fun signIn(email: String, password: String): AuthenticatedSession
    suspend fun signOut()
}
