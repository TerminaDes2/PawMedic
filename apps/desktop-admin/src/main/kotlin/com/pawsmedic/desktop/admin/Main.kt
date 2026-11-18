package com.pawsmedic.desktop.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.pawsmedic.desktop.admin.ui.SuperadminDashboardScreen
import com.pawsmedic.desktop.admin.ui.DesktopAppearanceProvider
import com.pawsmedic.desktop.admin.ui.SupportFooter
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import com.pawsmedic.shared.core.model.ProfileRole
import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

private fun loginText(spanish: String, english: Boolean): String {
    if (!english) return spanish
    return when (spanish) {
        "Inicia sesión con una cuenta Superadmin." -> "Sign in with a superadmin account."
        "Esta cuenta no tiene permisos de Superadmin." -> "This account does not have superadmin permissions."
        "Correo o contraseña incorrectos, o la cuenta no ha sido verificada." ->
            "Incorrect email or password, or the account has not been verified."
        "No se pudo iniciar sesión. Revisa tu conexión e inténtalo de nuevo." ->
            "Could not sign in. Check your connection and try again."
        "No se pudo validar la sesión." -> "Could not validate the session."
        "No se pudo cerrar la sesión." -> "Could not sign out."
        else -> spanish
    }
}

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    var authorizedEmail by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    suspend fun authorizeCurrentSession(): String {
        val user = supabase.auth.currentSessionOrNull()?.user
            ?: error("Inicia sesión con una cuenta Superadmin.")
        val profile = supabase.postgrest["profiles"]
            .select { filter { eq("id", user.id) } }
            .decodeSingle<UserProfile>()
        if (profile.role != ProfileRole.SUPERADMIN) {
            supabase.auth.signOut()
            error("Esta cuenta no tiene permisos de Superadmin.")
        }
        return profile.email
    }

    fun submitLogin() {
        if (isLoading || email.isBlank() || password.isBlank()) return
        isLoading = true
        coroutineScope.launch {
            errorMessage = null
            try {
                val cleanEmail = email.trim()
                val submittedPassword = password
                supabase.auth.signInWith(Email) {
                    this.email = cleanEmail
                    this.password = submittedPassword
                }
                authorizedEmail = authorizeCurrentSession()
                password = ""
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                val rawMsg = e.message.orEmpty()
                errorMessage = when {
                    rawMsg.contains("missing email or phone", ignoreCase = true) ||
                    rawMsg.contains("invalid_credentials", ignoreCase = true) ||
                    rawMsg.contains("invalid login", ignoreCase = true) ->
                        "Correo o contraseña incorrectos, o la cuenta no ha sido verificada."
                    else -> "No se pudo iniciar sesión. Revisa tu conexión e inténtalo de nuevo."
                }
            } finally {
                isLoading = false
            }
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Plataforma de Administración Central - Superadmin",
        state = windowState
    ) {
        DesktopAppearanceProvider {
            val appearance = LocalDesktopAppearance.current
            val englishLogin = appearance.english
            LaunchedEffect(Unit) {
                try {
                    val session = supabase.auth.currentSessionOrNull()
                    if (session != null) authorizedEmail = authorizeCurrentSession()
                } catch (e: Exception) {
                    errorMessage = "No se pudo validar la sesión."
                } finally {
                    isLoading = false
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
            if (authorizedEmail != null) {
                SuperadminDashboardScreen(
                    userEmail = authorizedEmail.orEmpty(),
                    onLogout = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                supabase.auth.signOut()
                                authorizedEmail = null
                                password = ""
                            } catch (e: Exception) {
                                errorMessage = "No se pudo cerrar la sesión."
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            } else if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().padding(28.dp).onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown && (it.key == Key.Enter || it.key == Key.NumPadEnter)) {
                                submitLogin()
                                true
                            } else false
                        },
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            if (englishLogin) "Superadmin sign in" else "Acceso Superadmin",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            if (englishLogin) "English is available for sign-in, navigation and reports."
                            else "Inglés disponible para acceso, navegación y reportes.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = appearance::toggleDark) {
                                Text(
                                    if (englishLogin) {
                                        if (appearance.dark) "Light mode" else "Dark mode"
                                    } else {
                                        if (appearance.dark) "Modo claro" else "Modo oscuro"
                                    }
                                )
                            }
                            TextButton(onClick = appearance::toggleLanguage) {
                                Text(if (englishLogin) "Español" else "English")
                            }
                        }
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it.trim(); errorMessage = null },
                            label = { Text(if (englishLogin) "Email" else "Correo electrónico") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text(if (englishLogin) "Password" else "Contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        errorMessage?.let { Text(loginText(it, englishLogin), color = MaterialTheme.colorScheme.error) }
                        Button(
                            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            onClick = ::submitLogin
                        ) {
                            Text(if (englishLogin) "Sign in" else "Iniciar sesión")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                SupportFooter(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
            }
            }
        }
    }
}
