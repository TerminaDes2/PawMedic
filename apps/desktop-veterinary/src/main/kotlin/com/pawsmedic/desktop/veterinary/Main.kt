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
import com.pawsmedic.desktop.veterinary.ui.AgendaScreen
import com.pawsmedic.desktop.veterinary.ui.CatalogScreen
import com.pawsmedic.desktop.veterinary.ui.ExpedientesScreen
import com.pawsmedic.desktop.veterinary.ui.auth.LoginScreen
import com.pawsmedic.desktop.veterinary.ui.auth.RegisterScreen
import com.pawsmedic.desktop.veterinary.ui.auth.SuperadminLoginScreen
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

@Serializable
private data class TenantState(
    @SerialName("status") val status: String = ""
)

@Composable
private fun AccessStatusScreen(
    message: String,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Estado de la cuenta",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBackToLogin,
            colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
        ) {
            Text("Volver al inicio de sesión", color = Color.White)
        }
    }
}

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    var showExitDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("Login") }
    var activeUserEmail by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val user = supabase.auth.currentSessionOrNull()?.user ?: return@LaunchedEffect
            val userId = user.id
            val email = user.email.orEmpty()
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingle<UserProfile>()
            activeUserEmail = email
            currentScreen = when (profile.role) {
                ProfileRole.SUPERADMIN -> "Admin"
                ProfileRole.VETERINARY_BUSINESS -> {
                    val tenantId = profile.tenantId
                    if (tenantId == null) {
                        "Admin"
                    } else {
                        val tenant = supabase.postgrest["tenants"]
                            .select { filter { eq("id", tenantId) } }
                            .decodeSingle<TenantState>()
                        if (tenant.status == "ACTIVE") "Agenda" else {
                            statusMessage = "La clínica no está activa. Contacta al Superadmin."
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

        } catch (e: Exception) {
            statusMessage = e.message ?: "No se pudo restaurar la sesión de Supabase."
            currentScreen = "AccessStatus"
        }
    }

    fun handleLogout() {
        coroutineScope.launch {
            SessionManager.clearSession()
            supabase.auth.signOut()
            activeUserEmail = ""
            statusMessage = ""
            currentScreen = "Login"
        }
    }

    Window(
        onCloseRequest = {
            if (currentScreen == "Agenda" || currentScreen == "Admin" || currentScreen == "Expedientes" || currentScreen == "Catalog") {
                showExitDialog = true
            } else {
                exitApplication()
            }
        },
        title = "PawsMedic - Sistema Veterinario",
        state = windowState
    ) {
        MaterialTheme {
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    "Login" -> LoginScreen(
                        onNavigateToVetMain = { email ->
                            activeUserEmail = email
                            currentScreen = "Agenda"
                        },
                        onNavigateToAdminLogin = {
                            currentScreen = "AdminLogin"
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
                    "Register" -> RegisterScreen(
                        onNavigateToVetMain = { currentScreen = "Agenda" },
                        onNavigateToLogin = { currentScreen = "Login" }
                    )
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
                    "Admin" -> SuperadminDashboardScreen(
                        userEmail = activeUserEmail,
                        onLogout = { handleLogout() },
                        supabaseClient = supabase
                    )
                    "AccessStatus" -> AccessStatusScreen(
                        message = statusMessage,
                        onBackToLogin = { handleLogout() }
                    )
                }

                // DIÁLOGO DE CONFIRMACIÓN DE SALIDA CON SESIÓN ACTIVA
                if (showExitDialog) {
                    Dialog(onDismissRequest = { showExitDialog = false }) {
                        Card(
                            modifier = Modifier
                                .width(540.dp)
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                    text = "Aviso: Sesión Activa",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Text(
                                    text = "No cerraste sesión. Cuando vuelvas a abrir el programa tu sesión estará activa.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showExitDialog = false },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Regresar", fontSize = 12.sp, color = TextPrimary)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                SessionManager.clearSession()
                                                supabase.auth.signOut()
                                                showExitDialog = false
                                                exitApplication()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                    ) {
                                        Text("Cerrar sesión y salir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            showExitDialog = false
                                            exitApplication()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                                    ) {
                                        Text("Continuar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
