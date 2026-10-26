package com.pawsmedic.features.auth.presentation.register

import com.pawsmedic.features.auth.domain.model.RegisterParams
import com.pawsmedic.features.auth.domain.usecase.LoginWithGoogleUseCase
import com.pawsmedic.features.auth.domain.usecase.RegisterUseCase
import com.pawsmedic.features.auth.domain.usecase.ResendOtpUseCase
import com.pawsmedic.features.auth.domain.usecase.VerifyOtpUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val requiresOtpVerification: Boolean = false,
    val otpCode: String = "",
    val resendSuccessMessage: String? = null,
    val isSuccess: Boolean = false,
    val userId: String? = null,
    val userEmail: String? = null
)

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase? = null,
    private val resendOtpUseCase: ResendOtpUseCase? = null,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChanged(value: String) {
        _uiState.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value.trim(), errorMessage = null) }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phone = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun onOtpCodeChanged(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(otpCode = value, errorMessage = null) }
        }
    }

    fun toggleTermsAccepted() {
        _uiState.update { it.copy(termsAccepted = !it.termsAccepted, errorMessage = null) }
    }

    fun onOtpNavigationHandled() {
        _uiState.update { it.copy(requiresOtpVerification = false) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun register() {
        val s = _uiState.value
        val email = s.email.trim()

        if (s.fullName.trim().length < 2) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu nombre completo") }
            return
        }
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu correo electrónico") }
            return
        }
        if (!EMAIL_REGEX.matches(email)) {
            _uiState.update { it.copy(errorMessage = "Ingresa un correo electrónico válido (ej. usuario@dominio.com)") }
            return
        }
        val phoneDigits = s.phone.filter(Char::isDigit)
        if (phoneDigits.length !in 8..15) {
            _uiState.update { it.copy(errorMessage = "Ingresa un teléfono válido (entre 8 y 15 dígitos)") }
            return
        }
        if (s.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres") }
            return
        }
        if (s.password != s.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }
        if (!s.termsAccepted) {
            _uiState.update { it.copy(errorMessage = "Debes aceptar los Términos y Condiciones") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                registerUseCase(
                    RegisterParams(
                        fullName = s.fullName,
                        email = email,
                        phone = s.phone,
                        password = s.password
                    )
                )
            }.onSuccess { result ->
                if (result.isVerified && result.session != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            userId = result.session.userId,
                            userEmail = result.session.email
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            requiresOtpVerification = true
                        )
                    }
                }
            }.onFailure { error ->
                val msg = when {
                    error.message?.contains("already registered", ignoreCase = true) == true ||
                    error.message?.contains("already exists", ignoreCase = true) == true ->
                        "Este correo electrónico ya está registrado. Intenta iniciar sesión."
                    else -> error.message ?: "Error al registrarse. Inténtalo de nuevo."
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = msg
                    )
                }
            }
        }
    }

    fun verifyOtp() {
        val s = _uiState.value
        val code = s.otpCode.trim()
        if (code.length < 6) {
            _uiState.update { it.copy(errorMessage = "Ingresa el código completo de 6 dígitos") }
            return
        }
        if (verifyOtpUseCase == null) {
            _uiState.update { it.copy(errorMessage = "La verificación de correo no está configurada") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                verifyOtpUseCase.invoke(email = s.email, code = code)
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
                        errorMessage = "El código ingresado es incorrecto o ha expirado. Verifica el código e inténtalo de nuevo."
                    )
                }
            }
        }
    }

    fun resendOtp() {
        val s = _uiState.value
        if (resendOtpUseCase == null) {
            _uiState.update { it.copy(errorMessage = "El reenvío de códigos no está configurado") }
            return
        }
        if (s.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa un correo electrónico válido") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, resendSuccessMessage = null) }
            runCatching {
                resendOtpUseCase.invoke(s.email)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resendSuccessMessage = "Se ha reenviado un nuevo código a tu correo."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al reenviar el código"
                    )
                }
            }
        }
    }

    fun registerWithGoogle(idToken: String, rawNonce: String? = null) {
        if (loginWithGoogleUseCase == null) {
            _uiState.update { it.copy(errorMessage = "El registro con Google no está configurado") }
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
                        errorMessage = error.message ?: "Error al registrarse con Google"
                    )
                }
            }

        }
    }

    fun resetAfterSignOut() {
        _uiState.value = RegisterUiState()
    }

    fun onGoogleSignInError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }
}
