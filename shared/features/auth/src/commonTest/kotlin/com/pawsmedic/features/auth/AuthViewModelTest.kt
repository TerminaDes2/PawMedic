package com.pawsmedic.features.auth

import com.pawsmedic.features.auth.data.datasource.NoOpSupabaseAuthDataSource
import com.pawsmedic.features.auth.data.repository.SupabaseAuthRepository
import com.pawsmedic.features.auth.domain.usecase.RestoreSessionUseCase
import com.pawsmedic.features.auth.presentation.AuthUiState
import com.pawsmedic.features.auth.presentation.AuthViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthViewModelTest {
    @Test
    fun restoreWithoutConfiguredSupabaseIsSignedOut() = runTest {
        val repository = SupabaseAuthRepository(NoOpSupabaseAuthDataSource())
        val viewModel = AuthViewModel(RestoreSessionUseCase(repository), repository, this)
        viewModel.restore()
        testScheduler.advanceUntilIdle()
        assertEquals(AuthUiState.SignedOut, viewModel.state.value)
    }
}
