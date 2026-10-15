package com.pawsmedic.features.auth.data.repository

import com.pawsmedic.features.auth.data.datasource.SupabaseAuthDataSource
import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.domain.model.RegisterParams
import com.pawsmedic.features.auth.domain.repository.AuthRepository
import com.pawsmedic.features.auth.domain.repository.SignUpResult

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
        fullName: String?,
        phone: String?
    ): SignUpResult {
        val resultDto = dataSource.signUp(email, password, role, fullName, phone)
        return SignUpResult(
            isVerified = resultDto.isVerified,
            session = resultDto.session?.toDomain()
        )
    }

    override suspend fun signUp(params: RegisterParams): SignUpResult {
        val resultDto = dataSource.signUp(params)
        return SignUpResult(
            isVerified = resultDto.isVerified,
            session = resultDto.session?.toDomain()
        )
    }

    override suspend fun verifyEmailOtp(email: String, code: String): AuthenticatedSession =
        dataSource.verifyEmailOtp(email, code).toDomain()

    override suspend fun resendEmailOtp(email: String) {
        dataSource.resendEmailOtp(email)
    }

    override suspend fun signInWithGoogleIdToken(
        idToken: String,
        rawNonce: String?
    ): AuthenticatedSession =
        dataSource.signInWithGoogleIdToken(idToken, rawNonce).toDomain()

    override suspend fun signOut() = dataSource.signOut()
}
