package com.pawsmedic.features.auth.data.repository

import com.pawsmedic.features.auth.data.datasource.SupabaseAuthDataSource
import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.repository.AuthRepository

class SupabaseAuthRepository(
    private val dataSource: SupabaseAuthDataSource
) : AuthRepository {
    override suspend fun restoreSession(): AuthenticatedSession? =
        dataSource.currentSession()?.toDomain()

    override suspend fun signIn(email: String, password: String): AuthenticatedSession =
        dataSource.signIn(email, password).toDomain()

    override suspend fun signOut() = dataSource.signOut()
}
