package com.pawsmedic.features.auth

import com.pawsmedic.core.data.InMemoryAuthRepository
import com.pawsmedic.features.auth.domain.AuthState
import com.pawsmedic.features.auth.presentation.AuthViewModel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs

class AuthViewModelTest {
    @Test
    fun signInPublishesSignedInState() = runTest {
        val viewModel = AuthViewModel(InMemoryAuthRepository(), backgroundScope)
        viewModel.signIn("owner@example.test", "password")
        advanceUntilIdle()
        assertIs<AuthState.SignedIn>(viewModel.state.value)
    }

    @Test
    fun invalidCredentialsPublishError() = runTest(StandardTestDispatcher()) {
        val viewModel = AuthViewModel(InMemoryAuthRepository(), backgroundScope)
        viewModel.signIn("", "")
        advanceUntilIdle()
        assertIs<AuthState.Error>(viewModel.state.value)
    }
}
