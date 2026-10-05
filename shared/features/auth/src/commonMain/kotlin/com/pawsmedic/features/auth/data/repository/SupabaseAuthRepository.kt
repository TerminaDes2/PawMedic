package com.pawsmedic.features.auth.data.repository

import com.pawsmedic.features.auth.data.datasource.SupabaseAuthDataSource
import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.domain.model.RegisterParams
import com.pawsmedic.features.auth.domain.repository.AuthRepository

class SupabaseAuthRepository(
    private val dataSource: SupabaseAuthDataSource
) : AuthRepository {

    override suspend fun restoreSession(): AuthenticatedSession? =
        dataSource.currentSession()?.toDomain()

    override suspend fun signIn(
        email: String,
        password: String,
        requiredRole: PawMedicRole?
    ): AuthenticatedSession {
        val session = dataSource.signIn(email, password).toDomain()

        if (requiredRole != null && session.role != requiredRole) {
            dataSource.signOut()
            throw IllegalArgumentException("Acceso denegado: Se requiere el rol ${requiredRole.name}")
        }

        return session
    }

    override suspend fun signUp(
        email: String,
        password: String,
        role: PawMedicRole,
        fullName: String?
    ): AuthenticatedSession {
        val sessionDto = dataSource.signUp(email, password, role, fullName)
        return sessionDto.toDomain()
    }

    override suspend fun signUp(params: RegisterParams): AuthenticatedSession =
        dataSource.signUp(params).toDomain()

    override suspend fun signOut() = dataSource.signOut()
}