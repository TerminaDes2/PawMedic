package com.pawsmedic.features.auth.presentation

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.repository.AuthRepository
import com.pawsmedic.features.auth.domain.usecase.RestoreSessionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data class Authenticated(val session: AuthenticatedSession) : AuthUiState
    data object SignedOut : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val restoreSession: RestoreSessionUseCase,
    private val repository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val mutableState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val state: StateFlow<AuthUiState> = mutableState.asStateFlow()

    fun restore() {
        scope.launch {
            runCatching { restoreSession() }
                .onSuccess { mutableState.value = it?.let(AuthUiState::Authenticated) ?: AuthUiState.SignedOut }
                .onFailure { mutableState.value = AuthUiState.Error(it.message ?: "Unable to restore session") }
        }
    }

    fun signIn(email: String, password: String) {
        scope.launch {
            mutableState.value = AuthUiState.Loading
            runCatching { repository.signIn(email, password) }
                .onSuccess { mutableState.value = AuthUiState.Authenticated(it) }
                .onFailure { mutableState.value = AuthUiState.Error(it.message ?: "Sign-in failed") }
        }
    }

    fun signOut() {
        scope.launch {
            runCatching { repository.signOut() }
                .onSuccess { mutableState.value = AuthUiState.SignedOut }
                .onFailure { mutableState.value = AuthUiState.Error(it.message ?: "Sign-out failed") }
        }
    }
}
