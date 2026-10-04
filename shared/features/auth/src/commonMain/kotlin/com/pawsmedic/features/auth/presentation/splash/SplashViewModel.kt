package com.pawsmedic.features.auth.presentation.splash

import com.pawsmedic.features.auth.domain.usecase.RestoreSessionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data object NavigateToWelcome : SplashUiState
    data class NavigateToHome(val userId: String) : SplashUiState
}

class SplashViewModel(
    private val restoreSession: RestoreSessionUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        scope.launch {
            _uiState.value = SplashUiState.Loading
            delay(1800) // Smooth splash display time as designed
            runCatching { restoreSession() }
                .onSuccess { session ->
                    if (session != null) {
                        _uiState.value = SplashUiState.NavigateToHome(session.userId)
                    } else {
                        _uiState.value = SplashUiState.NavigateToWelcome
                    }
                }
                .onFailure {
                    _uiState.value = SplashUiState.NavigateToWelcome
                }
        }
    }
}
