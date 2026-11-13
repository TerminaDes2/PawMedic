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

@Composable
fun VetAppLayout(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {},
    title: String,
    userEmail: String = "",
    content: @Composable BoxScope.() -> Unit
) {
    var showAdminModal by remember { mutableStateOf(false) }

    var adminUsername by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }
    var showProfile by remember(userEmail) { mutableStateOf(false) }
    var profileRevision by remember(userEmail) { mutableIntStateOf(0) }
    var profileName by remember(userEmail) { mutableStateOf("") }
    var avatar by remember(userEmail) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(userEmail, profileRevision) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@LaunchedEffect
            val profile = supabase.postgrest["profiles"].select { filter { eq("id", userId) } }
                .decodeSingle<ProfileAvatar>()
            profileName = profile.displayName.orEmpty()
            val url = profile.url ?: return@LaunchedEffect
            avatar = withContext(Dispatchers.IO) {
                val uri = URI(url)
                require(uri.scheme == "https") { "La foto debe usar HTTPS." }
                val connection = uri.toURL().openConnection().apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                connection.getInputStream().use { input -> ImageIO.read(input)?.toComposeImageBitmap() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            System.err.println("No se pudo cargar la foto del perfil.")
        }
    }

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
                Text("ESPACIO DE TRABAJO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))

                val menuItems = listOf(
                    Triple("Agenda", "Agenda y Citas", Icons.Default.CalendarMonth),
                    Triple("Horarios", "Horarios de atención", Icons.Default.Schedule),
                    Triple("Expedientes", "Expedientes Clínicos", Icons.Default.FolderShared),
                    Triple("Catalog", "Catálogo de Servicios", Icons.Default.ShoppingCart)
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
            }

            // CONTENIDO PRINCIPAL DERECHO
            Column(modifier = Modifier.fillMaxSize()) {
                // TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(CardWhite)
                        .border(1.dp, BorderLight)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "PANEL VETERINARIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald, letterSpacing = 1.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Perfil Usuario (Muestra icono y nombre antes de @)
                        val displayName = profileName.ifBlank { "Cuenta veterinaria" }
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
                                    val image = avatar
                                    if (image == null) {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null,
                                            tint = ClinicalEmerald, modifier = Modifier.size(20.dp))
                                    } else {
                                        Image(image, contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
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
                                        text = "Mi perfil",
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
                        .padding(20.dp)
                ) {
                    content()
                }
            }
        }

        if (showProfile) {
            AccountSidebar(
                avatar = avatar,
                onClose = { showProfile = false },
                onLogout = { showProfile = false; onLogout() },
                onProfileLoaded = { profileName = it },
                onProfileSaved = { name -> profileName = name; profileRevision++ }
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
