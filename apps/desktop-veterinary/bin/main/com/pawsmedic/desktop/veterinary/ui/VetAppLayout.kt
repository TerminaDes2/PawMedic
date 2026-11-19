package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.desktop.admin.ui.AppearanceButton
import com.pawsmedic.desktop.admin.ui.NavigationLanguageButton
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import com.pawsmedic.desktop.admin.ui.SupportFooter
import com.pawsmedic.desktop.veterinary.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI
import javax.imageio.ImageIO

@Serializable
private data class ProfileAvatar(
    @SerialName("avatar_url") val url: String? = null,
    @SerialName("display_name") val displayName: String? = null
)

private class HeaderProfile {
    var name by mutableStateOf("")
    var avatar by mutableStateOf<ImageBitmap?>(null)
    var revision by mutableIntStateOf(0)
}

private val LocalHeaderProfile = staticCompositionLocalOf { HeaderProfile() }

@Composable
internal fun HeaderProfileProvider(sessionKey: String, content: @Composable () -> Unit) {
    val state = remember(sessionKey) { HeaderProfile() }
    LaunchedEffect(sessionKey, state.revision) {
        if (sessionKey.isBlank()) return@LaunchedEffect
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@LaunchedEffect
            val profile = supabase.postgrest["profiles"].select { filter { eq("id", userId) } }
                .decodeSingle<ProfileAvatar>()
            state.name = profile.displayName.orEmpty()
            val url = profile.url
            if (url.isNullOrBlank()) {
                state.avatar = null
            } else {
                state.avatar = withContext(Dispatchers.IO) {
                    val uri = URI(url)
                    require(uri.scheme == "https") { "La foto debe usar HTTPS." }
                    val connection = uri.toURL().openConnection().apply {
                        connectTimeout = 5000
                        readTimeout = 5000
                    }
                    connection.getInputStream().use { input -> ImageIO.read(input)?.toComposeImageBitmap() }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            System.err.println("No se pudo cargar el perfil o su foto.")
        }
    }
    CompositionLocalProvider(LocalHeaderProfile provides state, content = content)
}

@Composable
fun VetAppLayout(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {},
    title: String,
    userEmail: String = "",
    content: @Composable BoxScope.() -> Unit
) {
    val english = LocalDesktopAppearance.current.english
    var showAdminModal by remember { mutableStateOf(false) }

    var adminUsername by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }
    var showProfile by remember(userEmail) { mutableStateOf(false) }
    val headerProfile = LocalHeaderProfile.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // SIDEBAR IZQUIERDO (260.dp)
            Column(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(DarkSlate)
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            ) {
                // Header Marca PawsMedic
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ClinicalEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Paws",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Medic",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClinicalEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
                Text(if (english) "WORKSPACE" else "ESPACIO DE TRABAJO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))

                val menuItems = listOf(
                    Triple("Agenda", if (english) "Appointments" else "Agenda y Citas", Icons.Default.CalendarMonth),
                    Triple("Horarios", if (english) "Opening hours" else "Horarios de atención", Icons.Default.Schedule),
                    Triple("Consultas", if (english) "Clinical consultations" else "Consultas Clínicas", Icons.Default.Healing),
                    Triple("Expedientes", if (english) "Medical records" else "Expedientes Clínicos", Icons.Default.FolderShared),
                    Triple("Catalog", if (english) "Catalog" else "Catálogo de Servicios", Icons.Default.ShoppingCart),
                    Triple("Reports", if (english) "Operational reports" else "Reportes operativos", Icons.Default.Assessment)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    menuItems.forEach { (id, label, icon) ->
                        val isSelected = currentScreen == id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ClinicalEmerald else Color.Transparent)
                                .clickable { onNavigate(id) }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
                SupportFooter(
                    labelColor = Color(0xFF94A3B8),
                    contactColor = Color(0xFFCBD5E1)
                )
            }

            // CONTENIDO PRINCIPAL DERECHO
            Column(modifier = Modifier.fillMaxSize()) {
                // TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(CardWhite)
                        .border(1.dp, BorderLight)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = if (english) "VETERINARY DASHBOARD" else "PANEL VETERINARIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald, letterSpacing = 1.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(text = if (english) when (currentScreen) {
                            "Agenda" -> "Appointments"
                            "Horarios" -> "Opening hours"
                            "Consultas" -> "Clinical consultations"
                            "Expedientes" -> "Medical records"
                            "Catalog" -> "Services and products"
                            "Reports" -> "Operational reports"
                            else -> title
                        } else title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AppearanceButton()
                        NavigationLanguageButton()
                        // Perfil Usuario (Muestra icono y nombre antes de @)
                        val displayName = headerProfile.name.ifBlank { if (english) "Veterinary account" else "Cuenta veterinaria" }
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .clickable { showProfile = true }.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldLightBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val image = headerProfile.avatar
                                    if (image == null) {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null,
                                            tint = ClinicalEmerald, modifier = Modifier.size(20.dp))
                                    } else {
                                        Image(image, contentDescription = if (english) "Profile photo" else "Foto de perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    }
                                }

                                Column {
                                    Text(
                                        text = displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (english) "My profile" else "Mi perfil",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                        }
                    }
                }

                // CONTENIDO DE PANTALLA
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    content()
                }
            }
        }

        if (showProfile) {
            AccountSidebar(
                avatar = headerProfile.avatar,
                onClose = { showProfile = false },
                onLogout = { showProfile = false; onLogout() },
                onProfileLoaded = { headerProfile.name = it },
                onProfileSaved = { name -> headerProfile.name = name; headerProfile.revision++ }
            )
        }

        // MODAL DE AUTENTICACIÓN ADMIN
        if (showAdminModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(480.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardWhite)
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                        .padding(28.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(EmeraldLightBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = ClinicalEmerald,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Autenticación Admin",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ingrese credenciales de administración para acceder.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        OutlinedTextField(
                            value = adminUsername,
                            onValueChange = { adminUsername = it },
                            label = { Text("Usuario") },
                            placeholder = { Text("admin@pawsmedic.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            isError = loginError
                        )

                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("Contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            isError = loginError
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showAdminModal = false
                                    adminUsername = ""
                                    adminPassword = ""
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancelar", color = TextPrimary)
                            }

                            Button(
                                onClick = {
                                    if (adminUsername.isNotEmpty() && adminPassword.isNotEmpty()) {
                                        showAdminModal = false
                                    } else {
                                        loginError = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                            ) {
                                Text("Iniciar Sesión", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlignedSearchBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(OffWhiteBg)
            .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 13.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
