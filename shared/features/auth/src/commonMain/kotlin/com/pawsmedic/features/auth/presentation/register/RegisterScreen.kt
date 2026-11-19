package com.pawsmedic.features.auth.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.shared.core.designsystem.components.GoogleSocialButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTextField
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.features.auth.presentation.terms.LegalSection
import com.pawsmedic.features.auth.presentation.terms.TermsAndConditionsDialog
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.features.auth.presentation.localizeAuthMessage

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    onGoogleSignInClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val uiState by viewModel.uiState.collectAsState()
    var legalSection by remember { mutableStateOf<LegalSection?>(null) }

    LaunchedEffect(uiState.isSuccess, uiState.userId) {
        if (uiState.isSuccess && uiState.userId != null) onRegisterSuccess(uiState.userId!!)
    }

    val stepOneValid = uiState.firstName.trim().length >= 2 && uiState.lastName.trim().length >= 2
    val stepTwoValid = uiState.username.matches(Regex("^[A-Za-z0-9._-]{3,30}$")) &&
        uiState.email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) &&
        uiState.phone.length == 10 && uiState.phone.all(Char::isDigit)
    val securityValid = uiState.password.length >= 6 && uiState.termsAccepted

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        PawMedicTopBar(
            title = language.text("Crear cuenta", "Create account"),
            subtitle = language.text("Regístrate para comenzar con PawMedic", "Sign up to get started with PawMedic"),
            onBackClick = if (uiState.step == 1) onBackClick else viewModel::backToIdentity
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = language.text("Paso ${uiState.step} de 3", "Step ${uiState.step} of 3"),
            color = PawMedicSemanticColors.current.accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { index ->
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = if (index < uiState.step) PawMedicColors.Teal600 else PawMedicSemanticColors.current.border,
                    thickness = 3.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))

        when (uiState.step) {
            1 -> {
                if (onGoogleSignInClick != null) {
                    GoogleSocialButton(
                        onClick = onGoogleSignInClick,
                        isLoading = uiState.isLoading,
                        text = language.text("Registrarse con Google", "Sign up with Google")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = PawMedicSemanticColors.current.border)
                        Text(language.text("  o regístrate con correo  ", "  or sign up with email  "), fontSize = 12.sp, color = PawMedicSemanticColors.current.hint)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = PawMedicSemanticColors.current.border)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                PawMedicTextField(
                    value = uiState.firstName,
                    onValueChange = viewModel::onFirstNameChanged,
                    label = language.text("Nombres", "First names"),
                    placeholder = language.text("Ej. Juan Carlos", "E.g. John"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) }
                )
                Spacer(modifier = Modifier.height(14.dp))
                PawMedicTextField(
                    value = uiState.lastName,
                    onValueChange = viewModel::onLastNameChanged,
                    label = language.text("Apellidos", "Last names"),
                    placeholder = language.text("Ej. Pérez López", "E.g. Smith"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) }
                )
                StepError(uiState.errorMessage?.let { localizeAuthMessage(it, language) })
                Spacer(modifier = Modifier.height(24.dp))
                PawMedicPrimaryButton(
                    text = language.text("Continuar", "Continue"),
                    onClick = viewModel::continueToAccount,
                    enabled = stepOneValid
                )
            }
            2 -> {
                PawMedicTextField(
                    value = uiState.username,
                    onValueChange = viewModel::onUsernameChanged,
                    label = language.text("Nombre de usuario", "Username"),
                    placeholder = "juansito",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) }
                )
                Spacer(modifier = Modifier.height(14.dp))
                PawMedicTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = language.text("Correo electrónico", "Email address"),
                    placeholder = "juanperez@correo.com",
                    inputModifier = Modifier.semantics { contentType = ContentType.EmailAddress },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) }
                )
                Spacer(modifier = Modifier.height(14.dp))
                PawMedicTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChanged,
                    label = language.text("Teléfono", "Phone number"),
                    placeholder = "111-222-3333",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) }
                )
                StepError(uiState.errorMessage?.let { localizeAuthMessage(it, language) })
                Spacer(modifier = Modifier.height(24.dp))
                PawMedicPrimaryButton(
                    text = language.text("Continuar", "Continue"),
                    onClick = viewModel::continueToSecurity,
                    enabled = stepTwoValid
                )
            }
            else -> {
                PawMedicTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = language.text("Contraseña", "Password"),
                    placeholder = language.text("Mínimo 6 caracteres", "At least 6 characters"),
                    inputModifier = Modifier.semantics { contentType = ContentType.NewPassword },
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (uiState.isPasswordVisible) language.text("Ocultar contraseña", "Hide password") else language.text("Mostrar contraseña", "Show password"),
                            tint = PawMedicSemanticColors.current.hint,
                            modifier = Modifier.size(20.dp).clickable { viewModel.togglePasswordVisibility() }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleTermsAccepted() }
                ) {
                    Checkbox(
                        checked = uiState.termsAccepted,
                        onCheckedChange = { viewModel.toggleTermsAccepted() },
                        colors = CheckboxDefaults.colors(checkedColor = PawMedicColors.Teal600, uncheckedColor = PawMedicSemanticColors.current.hint)
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                        Text(
                            text = language.text("He leído y acepto:", "I have read and accept:"),
                            fontSize = 13.sp,
                            color = PawMedicSemanticColors.current.text,
                            modifier = Modifier.clickable { viewModel.toggleTermsAccepted() }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = language.text("Términos de servicio", "Terms of service"),
                                fontSize = 12.sp,
                                color = PawMedicSemanticColors.current.accent,
                                modifier = Modifier.clickable { legalSection = LegalSection.TERMS }
                            )
                            Text("·", fontSize = 12.sp, color = PawMedicSemanticColors.current.hint)
                            Text(
                                text = language.text("Política de privacidad", "Privacy policy"),
                                fontSize = 12.sp,
                                color = PawMedicSemanticColors.current.accent,
                                modifier = Modifier.clickable { legalSection = LegalSection.PRIVACY }
                            )
                        }
                    }
                }
                StepError(uiState.errorMessage?.let { localizeAuthMessage(it, language) })
                Spacer(modifier = Modifier.height(24.dp))
                PawMedicPrimaryButton(
                    text = language.text("Crear cuenta", "Create account"),
                    onClick = viewModel::register,
                    enabled = securityValid,
                    isLoading = uiState.isLoading
                )
            }
        }

        legalSection?.let { section ->
            TermsAndConditionsDialog(section = section, onDismiss = { legalSection = null })
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(language.text("¿Ya tienes una cuenta? ", "Already have an account? "), fontSize = 14.sp, color = PawMedicSemanticColors.current.secondaryText)
            Text(
                language.text("Iniciar sesión", "Sign in"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.accent,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}

@Composable
private fun StepError(message: String?) {
    if (message != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, fontSize = 13.sp, color = PawMedicColors.Error)
    }
}
