package com.pawsmedic.desktop.veterinary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.pawsmedic.desktop.admin.ui.SuperadminDashboardScreen
import com.pawsmedic.desktop.admin.ui.DesktopAppearanceProvider
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import com.pawsmedic.desktop.admin.ui.AppearanceButton
import com.pawsmedic.desktop.admin.ui.NavigationLanguageButton
import com.pawsmedic.desktop.admin.ui.SupportFooter
import com.pawsmedic.desktop.admin.translation.translateLocally
import com.pawsmedic.desktop.veterinary.ui.AgendaScreen
import com.pawsmedic.desktop.veterinary.ui.CatalogScreen
import com.pawsmedic.desktop.veterinary.ui.CatalogTranslationProvider
import com.pawsmedic.desktop.veterinary.ui.ExpedientesScreen
import com.pawsmedic.desktop.veterinary.ui.HeaderProfileProvider
import com.pawsmedic.desktop.veterinary.ui.ScheduleScreen
import com.pawsmedic.desktop.veterinary.ui.ReportsScreen
import com.pawsmedic.desktop.veterinary.ui.auth.LoginScreen
import com.pawsmedic.desktop.veterinary.ui.auth.RegisterScreen
import com.pawsmedic.desktop.veterinary.ui.auth.SuperadminLoginScreen
import com.pawsmedic.desktop.veterinary.model.veterinaryAccessStatus
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.shared.core.model.ProfileRole
import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.launch

@Serializable
private data class BusinessApplicationState(
    @SerialName("status") val status: String = "",
    @SerialName("decision_reason") val decisionReason: String? = null
)

@Composable
private fun AccessStatusScreen(
    message: String,
    onBackToLogin: () -> Unit
) {
    val english = LocalDesktopAppearance.current.english
    val knownTranslation = remember(message) { knownAccessStatusEnglish(message) }
    var translation by remember(message) { mutableStateOf<String?>(null) }
    var showOriginal by remember(message) { mutableStateOf(false) }
    var translating by remember(message) { mutableStateOf(false) }
    var translationError by remember(message) { mutableStateOf(false) }
    var translationAttempt by remember(message) { mutableIntStateOf(0) }
    LaunchedEffect(message, english, translationAttempt) {
        if (!english || knownTranslation != null || message.isBlank() || translation != null) {
            translating = false
            return@LaunchedEffect
        }
        if (message.length > 4000) {
            translationError = true
            return@LaunchedEffect
        }
        translating = true
        translationError = false
        try {
            translation = translateLocally(message)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            translationError = true
        } finally {
            translating = false
        }
    }

    Box(Modifier.fillMaxSize().background(OffWhiteBg)) {
        Row(Modifier.align(Alignment.TopEnd).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AppearanceButton()
            NavigationLanguageButton()
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Text(
            text = if (english) "Account status" else "Estado de la cuenta",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (english && !showOriginal) knownTranslation ?: translation ?: message else message,
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        if (english && knownTranslation == null && message.isNotBlank()) {
            Text("Machine translation may be inaccurate. Verify the original.", fontSize = 12.sp, color = TextSecondary)
            TextButton(onClick = { showOriginal = !showOriginal }) { Text(if (showOriginal) "Show translation" else "Show original") }
            if (translating) Text("Translating…", color = TextSecondary, fontSize = 12.sp)
            if (translationError) {
                Text("Could not translate this message; the original is shown.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                TextButton(onClick = { translationAttempt++ }) { Text("Retry") }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBackToLogin,
            colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
        ) {
            Text(if (english) "Back to sign in" else "Volver al inicio de sesión", color = Color.White)
        }
        }
    }
}

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    var showExitDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("Restoring") }
    var activeUserEmail by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var sessionBusy by remember { mutableStateOf(false) }
    var exitError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            if (!SessionManager.isRememberMeEnabled()) {
                SecureSessionStorage.deleteSession()
                currentScreen = "Login"
                return@LaunchedEffect
            }
            supabase.auth.loadFromStorage()
            val user = supabase.auth.currentSessionOrNull()?.user
            if (user == null) {
                currentScreen = "Login"
                return@LaunchedEffect
            }
            val userId = user.id
            val email = user.email.orEmpty()
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingle<UserProfile>()
            activeUserEmail = email
            currentScreen = when (profile.role) {
                ProfileRole.SUPERADMIN -> "Admin"
                ProfileRole.VETERINARY_BUSINESS -> {
                    val app = veterinaryAccessStatus(supabase, userId)
                    when (app?.status) {
                        "APPROVED" -> "Agenda"
                        else -> {
                            statusMessage = when (app?.status) {
                                "PENDING" -> "Tu solicitud de registro veterinario está pendiente de aprobación por el Superadministrador."
                                "REJECTED" -> app.decisionReason ?: "Tu solicitud de registro fue rechazada por el Superadministrador."
                                "SUSPENDED" -> "Tu clínica está suspendida."
                                else -> "No se pudo confirmar la aprobación de tu solicitud veterinaria."
                            }
                            "AccessStatus"
                        }
                    }
                }
                ProfileRole.USER -> {
                    val application = supabase.postgrest["businesses"]
                        .select { filter { eq("submitted_by", userId) } }
                        .decodeList<BusinessApplicationState>()
                        .firstOrNull()
                    statusMessage = when (application?.status) {
                        "PENDING" -> "Tu solicitud de registro veterinario está pendiente de aprobación."
                        "REJECTED" -> application.decisionReason
                            ?: "Tu solicitud de registro veterinario fue rechazada."
                        else -> "Esta cuenta no tiene acceso a la aplicación de escritorio."
                    }
                    "AccessStatus"
                }
            }

        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            statusMessage = "No se pudo restaurar la sesión. Revisa tu conexión e inicia sesión de nuevo."
            currentScreen = "AccessStatus"
        }
    }

    fun handleLogout() {
        if (sessionBusy) return
        sessionBusy = true
        coroutineScope.launch {
            try {
                SessionManager.clearSession()
                SecureSessionStorage.deleteSession()
                supabase.auth.signOut()
                activeUserEmail = ""
                statusMessage = ""
                currentScreen = "Login"
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                statusMessage = "No se pudo cerrar la sesión. Revisa tu conexión e inténtalo de nuevo."
                currentScreen = "AccessStatus"
            } finally {
                sessionBusy = false
            }
        }
    }

    Window(
        onCloseRequest = {
            if (sessionBusy || currentScreen == "Restoring") {
                Unit
            } else if (SessionManager.isRememberMeEnabled() && supabase.auth.currentSessionOrNull() != null) {
                exitError = null
                showExitDialog = true
            } else {
                sessionBusy = true
                coroutineScope.launch {
                    try {
                        SessionManager.clearSession()
                        SecureSessionStorage.deleteSession()
                        supabase.auth.signOut()
                        exitApplication()
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        exitError = "No se pudo cerrar la sesión. Revisa tu conexión e inténtalo de nuevo."
                        showExitDialog = true
                    } finally {
                        sessionBusy = false
                    }
                }
            }
        },
        title = "PawsMedic - Sistema Veterinario",
        state = windowState
    ) {
        DesktopAppearanceProvider {
            HeaderProfileProvider(activeUserEmail) {
            CatalogTranslationProvider(activeUserEmail) {
            Box(modifier = Modifier.fillMaxSize()) {
                val english = LocalDesktopAppearance.current.english
                when (currentScreen) {
                    "Restoring" -> Box(Modifier.fillMaxSize().background(OffWhiteBg), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CircularProgressIndicator(color = ClinicalEmerald)
                            Text(if (english) "Preparing your session..." else "Preparando tu sesión...", color = TextSecondary)
                        }
                    }
                    "Login" -> LoginScreen(
                        onNavigateToVetMain = { email ->
                            activeUserEmail = email
                            currentScreen = "Agenda"
                        },
                        onNavigateToAdminLogin = {
                            currentScreen = "AdminLogin"
                        },
                        onNavigateToAccessStatus = { message ->
                            statusMessage = message
                            currentScreen = "AccessStatus"
                        },
                        onNavigateToRegister = { currentScreen = "Register" }
                    )
                    "AdminLogin" -> SuperadminLoginScreen(
                        onNavigateToAdminMain = { email ->
                            activeUserEmail = email
                            currentScreen = "Admin"
                        },
                        onBackToLogin = { currentScreen = "Login" }
                    )
                    "Register" -> RegisterScreen(onNavigateToLogin = { currentScreen = "Login" })
                    "Agenda" -> AgendaScreen(
                        userEmail = activeUserEmail,
                        onNavigate = { currentScreen = it },
                        onLogout = { handleLogout() }
                    )
                    "Expedientes" -> ExpedientesScreen(
                        userEmail = activeUserEmail,
                        onNavigate = { currentScreen = it },
                        onLogout = { handleLogout() }
                    )
                    "Catalog" -> CatalogScreen(
                        userEmail = activeUserEmail,
                        onNavigate = { currentScreen = it },
                        onLogout = { handleLogout() }
                    )
                    "Horarios" -> ScheduleScreen(
                        userEmail = activeUserEmail,
                        onNavigate = { currentScreen = it },
                        onLogout = { handleLogout() }
                    )
                    "Reports" -> ReportsScreen(
                        userEmail = activeUserEmail,
                        onNavigate = { currentScreen = it },
                        onLogout = { handleLogout() }
                    )
                    "Admin" -> SuperadminDashboardScreen(
                        userEmail = activeUserEmail,
                        onLogout = { handleLogout() },
                        supabaseClient = supabase
                    )
                    "AccessStatus" -> Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            AccessStatusScreen(message = statusMessage, onBackToLogin = { handleLogout() })
                        }
                        SupportFooter(modifier = Modifier.padding(start = 16.dp, bottom = 12.dp))
                    }
                }

                // DIÁLOGO DE CONFIRMACIÓN DE SALIDA CON SESIÓN ACTIVA
                if (showExitDialog) {
                    Dialog(onDismissRequest = { if (!sessionBusy) showExitDialog = false }) {
                        Card(
                            modifier = Modifier
                                .width(540.dp)
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                verticalArrangement = Arrangement.spacedBy(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(EmeraldLightBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ClinicalEmerald,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Text(
                                    text = if (english) "Active session" else "Aviso: Sesión Activa",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Text(
                                    text = if (english) "Choose whether to keep your session for the next time you open PawMedic."
                                        else "Elige si deseas conservar tu sesión para la próxima vez que abras PawMedic.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                exitError?.let {
                                    Text(if (english) "Could not sign out. Check your connection and try again." else it,
                                        color = Color(0xFFDC2626), fontSize = 12.sp)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showExitDialog = false },
                                        enabled = !sessionBusy,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (english) "Go back" else "Regresar", fontSize = 12.sp, color = TextPrimary)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            sessionBusy = true
                                            coroutineScope.launch {
                                                try {
                                                    SessionManager.clearSession()
                                                    SecureSessionStorage.deleteSession()
                                                    supabase.auth.signOut()
                                                    exitApplication()
                                                } catch (e: kotlinx.coroutines.CancellationException) {
                                                    throw e
                                                } catch (e: Exception) {
                                                    exitError = "No se pudo cerrar la sesión. Revisa tu conexión e inténtalo de nuevo."
                                                } finally {
                                                    sessionBusy = false
                                                }
                                            }
                                        },
                                        enabled = !sessionBusy,
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                    ) {
                                        Text(if (english) "Sign out and exit" else "Cerrar sesión y salir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            showExitDialog = false
                                            exitApplication()
                                        },
                                        enabled = !sessionBusy && SessionManager.isRememberMeEnabled(),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                                    ) {
                                        Text(if (english) "Continue" else "Continuar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
            }
        }
    }
}
