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
    val step: Int = 1,
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val termsAccepted: Boolean = false,
    val isPasswordVisible: Boolean = false,
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

    fun onFirstNameChanged(value: String) {
        val lettersAndSpaces = value.filter { it.isLetter() || it.isWhitespace() }
        _uiState.update { it.copy(firstName = lettersAndSpaces, errorMessage = null) }
    }

    fun onLastNameChanged(value: String) {
        val lettersAndSpaces = value.filter { it.isLetter() || it.isWhitespace() }
        _uiState.update { it.copy(lastName = lettersAndSpaces, errorMessage = null) }
    }

    fun onUsernameChanged(value: String) {
        val allowedCharacters = value.filter { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }
        _uiState.update { it.copy(username = allowedCharacters.take(30), errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value.trim(), errorMessage = null) }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phone = value.filter(Char::isDigit).take(10), errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
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

    fun continueToAccount() {
        val s = _uiState.value
        val error = when {
            s.firstName.trim().length < 2 -> "Ingresa tus nombres"
            s.lastName.trim().length < 2 -> "Ingresa tus apellidos"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
        } else {
            _uiState.update { it.copy(step = 2, errorMessage = null) }
        }
    }

    fun continueToSecurity() {
        val s = _uiState.value
        val email = s.email.trim()
        val phoneDigits = s.phone.filter(Char::isDigit)
        val error = when {
            !s.username.matches(Regex("^[A-Za-z0-9._-]{3,30}$")) ->
                "El nombre de usuario debe tener entre 3 y 30 caracteres"
            !EMAIL_REGEX.matches(email) -> "Ingresa un correo electrónico válido"
            phoneDigits.length != 10 -> "Ingresa un teléfono válido de 10 dígitos"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
        } else {
            _uiState.update { it.copy(step = 3, errorMessage = null) }
        }
    }

    fun backToIdentity() {
        _uiState.update { it.copy(step = (_uiState.value.step - 1).coerceAtLeast(1), errorMessage = null) }
    }

    fun register() {
        val s = _uiState.value
        val email = s.email.trim()

        if (s.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres") }
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
                        firstName = s.firstName.trim(),
                        lastName = s.lastName.trim(),
                        username = s.username,
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

    fun onError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    fun onGoogleSignInError(message: String) {
        onError(message)
    }
}
