package com.pawsmedic.features.auth.presentation.recovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTextField
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.features.auth.presentation.localizeAuthMessage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal fun createRecoveryClient(supabase: SupabaseClient): SupabaseClient =
    createSupabaseClient(supabase.supabaseHttpUrl, supabase.supabaseKey) {
        install(Auth) { minimalSettings() }
    }

@Composable
fun PasswordRecoveryScreen(
    supabase: SupabaseClient,
    initialEmail: String,
    onBack: () -> Unit
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val scope = rememberCoroutineScope()
    val recoveryClient = remember(supabase) {
        createRecoveryClient(supabase)
    }

    DisposableEffect(recoveryClient) {
        onDispose {
            CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                recoveryClient.close()
            }
        }
    }
    var email by remember { mutableStateOf(initialEmail.trim()) }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(false) }
    var complete by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun leave() {
        if (loading) return
        if (!verified) {
            onBack()
        } else {
            scope.launch {
                loading = true
                try {
                    recoveryClient.auth.signOut()
                    onBack()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    error = "No se pudo cerrar la sesión de recuperación. Verifica tu conexión."
                } finally {
                    loading = false
                }
            }
        }
    }

    RecoveryBackHandler(onBack = ::leave)

    Column(
        modifier = Modifier.fillMaxSize().background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        PawMedicTopBar(title = language.text("Recuperar contraseña", "Recover password"), onBackClick = ::leave)
        Spacer(Modifier.height(20.dp))
        if (complete) {
            Text(language.text("Tu contraseña se actualizó. Vuelve a iniciar sesión.", "Your password has been updated. Sign in again."))
            error?.let { Spacer(Modifier.height(16.dp)); Text(localizeAuthMessage(it, language), color = PawMedicColors.Error) }
            Spacer(Modifier.height(16.dp))
            PawMedicPrimaryButton(text = language.text("Volver al inicio de sesión", "Back to sign in"), onClick = ::leave, isLoading = loading)
        } else {
            PawMedicTextField(
                value = email,
                onValueChange = { email = it; error = null },
                label = language.text("Correo electrónico", "Email address"),
                placeholder = "tu@email.com",
                enabled = !sent && !loading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                inputModifier = Modifier.semantics { contentType = ContentType.EmailAddress }
            )
            if (sent) {
                Spacer(Modifier.height(16.dp))
                Text(language.text("Si el correo está registrado, recibirás un código de recuperación.", "If the email is registered, you will receive a recovery code."))
                Spacer(Modifier.height(16.dp))
                PawMedicTextField(
                    value = code,
                    onValueChange = { code = it.filter(Char::isDigit); error = null },
                    label = language.text("Código de recuperación", "Recovery code"),
                    placeholder = language.text("Código recibido", "Code received"),
                    enabled = !verified && !loading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(16.dp))
                PawMedicTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = language.text("Nueva contraseña", "New password"),
                    placeholder = language.text("Nueva contraseña", "New password"),
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    inputModifier = Modifier.semantics { contentType = ContentType.NewPassword }
                )
                Spacer(Modifier.height(16.dp))
                PawMedicTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it; error = null },
                    label = language.text("Confirmar contraseña", "Confirm password"),
                    placeholder = language.text("Repite la contraseña", "Repeat password"),
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation()
                )
            }
            error?.let { Spacer(Modifier.height(16.dp)); Text(localizeAuthMessage(it, language), color = PawMedicColors.Error) }
            Spacer(Modifier.height(24.dp))
            PawMedicPrimaryButton(
                text = if (sent) language.text("Cambiar contraseña", "Change password") else language.text("Enviar código", "Send code"),
                isLoading = loading,
                onClick = {
                    val validationError = if (sent) {
                        recoveryPasswordError(code, verified, password, confirmation)
                    } else {
                        recoveryEmailError(email)
                    }
                    when {
                        loading -> Unit
                        validationError != null -> error = validationError
                        else -> scope.launch {
                            loading = true
                            error = null
                            try {
                                if (!sent) {
                                    recoveryClient.auth.resetPasswordForEmail(email.trim())
                                    sent = true
                                } else {
                                    if (!verified) {
                                        recoveryClient.auth.verifyEmailOtp(
                                            type = OtpType.Email.RECOVERY,
                                            email = email.trim(),
                                            token = code
                                        )
                                        verified = true
                                    }
                                    recoveryClient.auth.updateUser { this.password = password }
                                    password = ""
                                    confirmation = ""
                                    complete = true
                                    recoveryClient.auth.signOut()
                                    verified = false
                                }
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                error = when {
                                    !sent -> "No se pudo solicitar el código. Inténtalo de nuevo."
                                    !verified -> "No se pudo verificar el código. Compruébalo e inténtalo de nuevo."
                                    complete -> "La contraseña se actualizó, pero no se pudo cerrar la sesión de recuperación."
                                    else -> "No se pudo actualizar la contraseña. Inténtalo de nuevo."
                                }
                            } finally {
                                loading = false
                            }
                        }
                    }
                }
            )
            if (sent && !verified) {
                TextButton(onClick = { sent = false; code = ""; error = null }, enabled = !loading) {
                    Text(language.text("Usar otro correo o solicitar otro código", "Use another email or request another code"))
                }
            }
        }
    }
}
