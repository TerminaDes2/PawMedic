package com.pawsmedic.features.auth.presentation

import com.pawsmedic.core.data.AuthRepository
import com.pawsmedic.core.model.AuthCredentials
import com.pawsmedic.features.auth.domain.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _state = MutableStateFlow<AuthState>(AuthState.SignedOut)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun signIn(email: String, password: String) {
        scope.launch {
            _state.value = AuthState.Loading
            repository.signIn(AuthCredentials(email, password))
                .onSuccess { _state.value = AuthState.SignedIn(it) }
                .onFailure { _state.value = AuthState.Error(it.message ?: "Unable to sign in") }
        }
    }

    fun restoreSession() {
        scope.launch {
            repository.currentSession()?.let { _state.value = AuthState.SignedIn(it) }
        }
    }

    fun signOut() {
        scope.launch {
            repository.signOut()
            _state.value = AuthState.SignedOut
        }
    }
}
