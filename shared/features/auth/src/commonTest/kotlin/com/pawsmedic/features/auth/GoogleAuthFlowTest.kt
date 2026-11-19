package com.pawsmedic.features.auth

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.domain.model.RegisterParams
import com.pawsmedic.features.auth.domain.repository.AuthRepository
import com.pawsmedic.features.auth.domain.repository.SignUpResult
import com.pawsmedic.features.auth.domain.usecase.LoginUseCase
import com.pawsmedic.features.auth.domain.usecase.LoginWithGoogleUseCase
import com.pawsmedic.features.auth.domain.usecase.RegisterUseCase
import com.pawsmedic.features.auth.presentation.login.LoginViewModel
import com.pawsmedic.features.auth.presentation.register.RegisterViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoogleAuthFlowTest {
    @Test
    fun registrationRequiresConsentBeforeAndAfterGooglePicker() = runTest {
        val repository = FakeRepository()
        val viewModel = RegisterViewModel(RegisterUseCase(repository), loginWithGoogleUseCase = LoginWithGoogleUseCase(repository), scope = this)
        assertFalse(viewModel.canStartGoogleRegistration())
        viewModel.registerWithGoogle("token", "nonce")
        testScheduler.advanceUntilIdle()
        assertEquals(0, repository.googleCalls)
        assertEquals("Debes aceptar los Términos y Condiciones", viewModel.uiState.value.errorMessage)
        viewModel.toggleTermsAccepted()
        assertTrue(viewModel.canStartGoogleRegistration())
        viewModel.toggleTermsAccepted()
        viewModel.registerWithGoogle("token", "nonce")
        assertEquals(0, repository.googleCalls)
    }

    @Test
    fun registrationForwardsNonceAndRecordsAcceptance() = runTest {
        val repository = FakeRepository()
        var versions: Pair<String, String>? = null
        val viewModel = RegisterViewModel(
            RegisterUseCase(repository),
            loginWithGoogleUseCase = LoginWithGoogleUseCase(repository),
            recordLegalAcceptance = { terms, privacy -> versions = terms to privacy },
            scope = this
        )
        viewModel.toggleTermsAccepted()
        viewModel.registerWithGoogle("token", "nonce")
        testScheduler.advanceUntilIdle()
        assertEquals("token" to "nonce", repository.lastGoogleCredentials)
        assertEquals("1.0" to "1.0", versions)
        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun registrationDoesNotReportSuccessWhenAcceptanceFails() = runTest {
        val repository = FakeRepository()
        val viewModel = RegisterViewModel(
            RegisterUseCase(repository),
            loginWithGoogleUseCase = LoginWithGoogleUseCase(repository),
            recordLegalAcceptance = { _, _ -> error("unavailable") },
            scope = this
        )
        viewModel.toggleTermsAccepted()
        viewModel.registerWithGoogle("token", "nonce")
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSuccess)
        assertEquals("No se pudo guardar la aceptación de los términos. Inténtalo nuevamente.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun loginForwardsCredentialsAndReportsFailures() = runTest {
        val repository = FakeRepository()
        val viewModel = LoginViewModel(LoginUseCase(repository), LoginWithGoogleUseCase(repository), this)
        viewModel.loginWithGoogle("token", "nonce")
        testScheduler.advanceUntilIdle()
        assertEquals("token" to "nonce", repository.lastGoogleCredentials)
        assertTrue(viewModel.uiState.value.isSuccess)

        val failingViewModel = LoginViewModel(LoginUseCase(repository), LoginWithGoogleUseCase(repository), this)
        failingViewModel.loginWithGoogle("", "nonce")
        testScheduler.advanceUntilIdle()
        assertFalse(failingViewModel.uiState.value.isSuccess)
        assertEquals("No se pudo obtener el token de Google", failingViewModel.uiState.value.errorMessage)
        assertEquals(1, repository.googleCalls)
    }

    private class FakeRepository : AuthRepository {
        var googleCalls = 0
        var lastGoogleCredentials: Pair<String, String?>? = null
        private val session = AuthenticatedSession("access-token", "user-id", "user@example.com", PawMedicRole.USER)

        override suspend fun restoreSession() = session
        override suspend fun signIn(email: String, password: String, requiredRole: PawMedicRole?) = session
        override suspend fun signUp(email: String, password: String, role: PawMedicRole, fullName: String?, phone: String?) = SignUpResult(false)
        override suspend fun signUp(params: RegisterParams) = SignUpResult(false)
        override suspend fun verifyEmailOtp(email: String, code: String) = session
        override suspend fun resendEmailOtp(email: String) = Unit
        override suspend fun recordLegalAcceptance(termsVersion: String, privacyVersion: String) = Unit
        override suspend fun signInWithGoogleIdToken(idToken: String, rawNonce: String?): AuthenticatedSession {
            googleCalls++
            lastGoogleCredentials = idToken to rawNonce
            return session
        }
        override suspend fun signOut() = Unit
    }
}
