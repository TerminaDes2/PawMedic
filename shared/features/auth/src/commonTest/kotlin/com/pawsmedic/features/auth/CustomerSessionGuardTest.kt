package com.pawsmedic.features.auth

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.model.LoginParams
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.domain.model.RegisterParams
import com.pawsmedic.features.auth.domain.repository.AuthRepository
import com.pawsmedic.features.auth.domain.repository.SignUpResult
import com.pawsmedic.features.auth.domain.usecase.LoginUseCase
import com.pawsmedic.features.auth.domain.usecase.LoginWithGoogleUseCase
import com.pawsmedic.features.auth.domain.usecase.RestoreSessionUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CustomerSessionGuardTest {
    @Test
    fun passwordLoginRejectsNonCustomerRole() = runTest {
        val repository = FakeAuthRepository(PawMedicRole.VETERINARY_BUSINESS)

        assertFailsWith<IllegalArgumentException> {
            LoginUseCase(repository)(LoginParams("vet@example.com", "password"))
        }
        assertEquals(1, repository.signOutCount)
    }

    @Test
    fun googleLoginRejectsNonCustomerRole() = runTest {
        val repository = FakeAuthRepository(PawMedicRole.SUPERADMIN)

        assertFailsWith<IllegalArgumentException> {
            LoginWithGoogleUseCase(repository)("google-token")
        }
        assertEquals(1, repository.signOutCount)
    }

    @Test
    fun restoringNonCustomerSessionSignsOut() = runTest {
        val repository = FakeAuthRepository(PawMedicRole.VETERINARY_BUSINESS)

        assertFailsWith<IllegalArgumentException> {
            RestoreSessionUseCase(repository)()
        }
        assertEquals(1, repository.signOutCount)
    }

    private class FakeAuthRepository(
        private val role: PawMedicRole
    ) : AuthRepository {
        var signOutCount = 0
            private set

        private val session = AuthenticatedSession(
            accessToken = "test-token",
            userId = "test-user",
            email = "test@example.com",
            role = role
        )

        override suspend fun restoreSession() = session

        override suspend fun signIn(
            email: String,
            password: String,
            requiredRole: PawMedicRole?
        ) = session

        override suspend fun signUp(
            email: String,
            password: String,
            role: PawMedicRole,
            fullName: String?,
            phone: String?
        ) = SignUpResult(isVerified = false)

        override suspend fun signUp(params: RegisterParams) = SignUpResult(isVerified = false)

        override suspend fun verifyEmailOtp(email: String, code: String) = session

        override suspend fun resendEmailOtp(email: String) = Unit

        override suspend fun signInWithGoogleIdToken(idToken: String, rawNonce: String?) = session

        override suspend fun signOut() {
            signOutCount++
        }
    }
}
