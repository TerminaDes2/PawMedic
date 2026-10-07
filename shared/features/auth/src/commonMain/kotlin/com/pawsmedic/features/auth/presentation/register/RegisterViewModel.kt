package com.pawsmedic.features.auth.presentation.register

import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.domain.model.RegisterParams
import com.pawsmedic.features.auth.domain.usecase.RegisterUseCase
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
    val selectedRole: PawMedicRole = PawMedicRole.USER,
    val termsAccepted: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val userId: String? = null
)

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChanged(value: String) {
        _uiState.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPhoneChanged(value: String) {
        val phoneCharacters = value.filter {
            it.isDigit() || it in "+- ()"
        }
        _uiState.update { it.copy(phone = phoneCharacters, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun onRoleSelected(role: PawMedicRole) {
        _uiState.update { it.copy(selectedRole = role, errorMessage = null) }
    }

    fun toggleTermsAccepted() {
        _uiState.update { it.copy(termsAccepted = !it.termsAccepted, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun register() {
        val s = _uiState.value
        val cleanFullName = s.fullName.trim()
        val cleanEmail = s.email.trim()
        val cleanPhone = s.phone
            .filter { it.isDigit() || it in "+- ()" }
            .trim()

        if (cleanFullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu nombre completo") }
            return
        }
        if (cleanEmail.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu correo electrónico") }
            return
        }
        if (cleanPhone.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu número de teléfono") }
            return
        }
        if (!cleanPhone.any(Char::isDigit)) {
            _uiState.update { it.copy(errorMessage = "El teléfono debe contener números") }
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
                        fullName = cleanFullName,
                        email = cleanEmail,
                        phone = cleanPhone,
                        password = s.password,
                        role = s.selectedRole
                    )
                )
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        userId = session.userId
                    )
                }
            }.onFailure { error ->
                val rawMsg = error.message.orEmpty()
                val userFriendlyMessage = if (
                    rawMsg.lowercase().contains("unable to resolve host") ||
                    rawMsg.lowercase().contains("no address associated") ||
                    rawMsg.lowercase().contains("unknownhost") ||
                    rawMsg.lowercase().contains("connection timed out") ||
                    rawMsg.lowercase().contains("request timeout") ||
                    rawMsg.lowercase().contains("timeout") ||
                    rawMsg.lowercase().contains("eai_nodata") ||
                    rawMsg.lowercase().contains("eai_again") ||
                    rawMsg.lowercase().contains("verifica tu conexión")
                ) {
                    "La conexión con Supabase tardó demasiado. Verifica tu internet e inténtalo de nuevo."
                } else {
                    rawMsg.ifBlank { "Error al registrarse. Inténtalo de nuevo." }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = userFriendlyMessage
                    )
                }
            }
        }
    }
}
