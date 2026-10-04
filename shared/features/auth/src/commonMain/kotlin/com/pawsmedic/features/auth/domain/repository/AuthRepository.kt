package com.pawsmedic.features.auth.domain.repository

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.model.LoginParams
import com.pawsmedic.features.auth.domain.model.RegisterParams

interface AuthRepository {
    suspend fun restoreSession(): AuthenticatedSession?
    suspend fun signIn(email: String, password: String): AuthenticatedSession
    suspend fun signIn(params: LoginParams): AuthenticatedSession = signIn(params.email, params.password)
    suspend fun signUp(params: RegisterParams): AuthenticatedSession
    suspend fun signOut()
}
