package com.pawsmedic.features.auth.presentation.login

import com.pawsmedic.features.auth.domain.model.LoginParams
import com.pawsmedic.features.auth.domain.usecase.LoginUseCase
import com.pawsmedic.features.auth.domain.usecase.LoginWithGoogleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val userId: String? = null,
    val userEmail: String? = null
)

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value.trim(), errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val currentState = _uiState.value
        val email = currentState.email.trim()
        val password = currentState.password

        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu correo electrónico") }
            return
        }
        if (!EMAIL_REGEX.matches(email)) {
            _uiState.update { it.copy(errorMessage = "Ingresa un correo electrónico válido (ej. usuario@dominio.com)") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu contraseña") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                loginUseCase(LoginParams(email = email, password = password))
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        userId = session.userId,
                        userEmail = session.email
                    )
                }
            }.onFailure { error ->
                val message = when {
                    error.message?.contains("Invalid login credentials", ignoreCase = true) == true ->
                        "El correo o la contraseña son incorrectos"
                    error.message?.contains("Email not confirmed", ignoreCase = true) == true ->
                        "Tu correo electrónico no ha sido verificado aún. Por favor confirma tu cuenta"
                    else -> error.message ?: "Error al iniciar sesión. Verifica tu conexión e inténtalo de nuevo."
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = message
                    )
                }
            }
        }
    }

    fun loginWithGoogle(idToken: String, rawNonce: String? = null) {
        if (loginWithGoogleUseCase == null) {
            _uiState.update { it.copy(errorMessage = "El inicio de sesión con Google no está configurado") }
            return
        }
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                loginWithGoogleUseCase.invoke(idToken, rawNonce)
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        userId = session.userId,
                        userEmail = session.email
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al iniciar sesión con Google"
                    )
                }
            }

        }
    }

    fun onGoogleSignInError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }
}
