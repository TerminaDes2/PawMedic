package com.pawsmedic.desktop.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.pawsmedic.desktop.admin.ui.SuperadminDashboardScreen
import com.pawsmedic.shared.core.model.ProfileRole
import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

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

    Window(
        onCloseRequest = ::exitApplication,
        title = "Plataforma de Administración Central - Superadmin",
        state = windowState
    ) {
        MaterialTheme {
            LaunchedEffect(Unit) {
                try {
                    val session = supabase.auth.currentSessionOrNull()
                    if (session != null) authorizedEmail = authorizeCurrentSession()
                } catch (e: Exception) {
                    errorMessage = e.message ?: "No se pudo validar la sesión."
                } finally {
                    isLoading = false
                }
            }

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
                                errorMessage = e.message ?: "No se pudo cerrar la sesión."
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            } else if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Acceso Superadmin", style = MaterialTheme.typography.headlineMedium)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it.trim(); errorMessage = null },
                            label = { Text("Correo electrónico") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("Contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Button(
                            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
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
                                    } catch (e: Exception) {
                                        val rawMsg = e.message.orEmpty()
                                        errorMessage = when {
                                            rawMsg.contains("missing email or phone", ignoreCase = true) ||
                                            rawMsg.contains("invalid_credentials", ignoreCase = true) ||
                                            rawMsg.contains("invalid login", ignoreCase = true) ->
                                                "Correo o contraseña incorrectos, o la cuenta no ha sido verificada."
                                            else -> rawMsg.ifBlank { "No se pudo iniciar sesión." }
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        ) {
                            Text("Iniciar sesión")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
