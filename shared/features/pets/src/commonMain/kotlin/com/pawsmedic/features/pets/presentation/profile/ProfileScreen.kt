package com.pawsmedic.features.pets.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.pets.domain.model.Pet
import com.pawsmedic.features.pets.presentation.components.PetAvatarImage
import com.pawsmedic.shared.core.common.rememberPhotoPicker
import com.pawsmedic.shared.core.designsystem.components.PawMedicTextField
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.features.pets.presentation.localizePetMessage
import io.github.jan.supabase.SupabaseClient

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    supabaseClient: SupabaseClient,
    pets: List<Pet>,
    appointmentsCount: Int,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSignOut: () -> Unit,
    drawerOnly: Boolean = false,
    showSidebar: Boolean = true,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val state by viewModel.uiState.collectAsState()
    val profile = state.profile
    var isEditingUsername by remember(profile?.username) { mutableStateOf(false) }
    var usernameDraft by remember(profile?.username) { mutableStateOf(profile?.username.orEmpty()) }
    val photoPicker = rememberPhotoPicker(
        supabaseClient = supabaseClient,
        bucketName = "profiles",
        folderName = viewModel.userId,
        onPhotoPicked = viewModel::updatePhotoUrl,
        onUploadError = viewModel::onError
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Sidebar lateral con acciones
        if (showSidebar) Column(
            modifier = Modifier
                .width(if (drawerOnly) 300.dp else 132.dp)
                .fillMaxSize()
                .background(PawMedicSemanticColors.current.surface)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "PawMedic",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.accent
            )
            ProfileSidebarItem(Icons.Default.Person, language.text("Perfil", "Profile"), selected = !drawerOnly) { onProfileClick() }
            ProfileSidebarItem(Icons.Default.Notifications, language.text("Notificaciones", "Notifications")) { onNotificationsClick() }
            ProfileSidebarItem(Icons.Default.Settings, language.text("Configuración", "Settings")) { onSettingsClick() }
            Spacer(modifier = Modifier.weight(1f))
            ProfileSidebarItem(Icons.AutoMirrored.Filled.Logout, language.text("Salir", "Sign out"), onClick = onSignOut)
        }

        // Contenido principal del perfil
        if (!drawerOnly) Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            PawMedicTopBar(
                title = language.text("Mi perfil", "My profile"),
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card superior: Foto, Nombre completo y Username
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PawMedicSemanticColors.current.surface)
                    .padding(vertical = 16.dp, horizontal = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .clickable { photoPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    PetAvatarImage(
                        photo_url = profile?.avatarUrl,
                        species = null,
                        size = 90.dp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = language.text("Cambiar foto", "Change photo"),
                    color = PawMedicSemanticColors.current.accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { photoPicker() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = listOf(profile?.firstName.orEmpty(), profile?.lastName.orEmpty())
                        .joinToString(" ")
                        .trim()
                        .ifBlank { profile?.displayName?.ifBlank { language.text("Usuario", "User") } ?: language.text("Usuario", "User") },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.text,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (isEditingUsername) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PawMedicTextField(
                            value = usernameDraft,
                            onValueChange = { usernameDraft = it },
                            label = "Username",
                            placeholder = "tu_username",
                            modifier = Modifier.width(160.dp)
                        )
                        IconButton(
                            onClick = {
                                viewModel.updateUsername(usernameDraft)
                                isEditingUsername = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = language.text("Guardar username", "Save username"),
                                tint = PawMedicSemanticColors.current.accent
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${profile?.username?.ifBlank { "usuario" } ?: "usuario"}",
                            color = PawMedicSemanticColors.current.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = {
                            usernameDraft = profile?.username.orEmpty()
                            isEditingUsername = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = language.text("Editar username", "Edit username"),
                                tint = PawMedicSemanticColors.current.accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Estadísticas (Mascotas y Citas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStat(language.text("Mascotas", "Pets"), pets.size, Modifier.weight(1f))
                ProfileStat(language.text("Citas", "Appointments"), appointmentsCount, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información de la Cuenta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PawMedicSemanticColors.current.surface)
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(
                    text = language.text("Información de la cuenta", "Account information"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.text
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProfileInfoRow(language.text("Correo electrónico", "Email address"), profile?.email.orEmpty())
                HorizontalDivider(color = PawMedicSemanticColors.current.mutedSurface, modifier = Modifier.padding(vertical = 6.dp))
                ProfileInfoRow(language.text("Teléfono", "Phone"), profile?.phone ?: language.text("No registrado", "Not recorded"))
                HorizontalDivider(color = PawMedicSemanticColors.current.mutedSurface, modifier = Modifier.padding(vertical = 6.dp))
                ProfileInfoRow(
                    language.text("Fecha de registro", "Registration date"),
                    profile?.createdAt?.takeIf { it.isNotBlank() }?.substringBefore("T") ?: language.text("No disponible", "Not available")
                )
                HorizontalDivider(color = PawMedicSemanticColors.current.mutedSurface, modifier = Modifier.padding(vertical = 6.dp))
                ProfileInfoRow(language.text("Ubicación", "Location"), language.text("No disponible", "Not available"))
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = localizePetMessage(state.errorMessage.orEmpty(), language),
                    color = PawMedicColors.Error,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileSidebarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (selected) PawMedicSemanticColors.current.softAccent else PawMedicSemanticColors.current.surface)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) PawMedicColors.Teal600 else PawMedicSemanticColors.current.secondaryText,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = if (selected) PawMedicSemanticColors.current.accent else PawMedicSemanticColors.current.text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, color = PawMedicSemanticColors.current.hint)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value.ifBlank { language.text("No disponible", "Not available") },
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = PawMedicSemanticColors.current.text
        )
    }
}

@Composable
private fun ProfileStat(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PawMedicSemanticColors.current.surface)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicSemanticColors.current.accent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PawMedicSemanticColors.current.secondaryText
        )
    }
}
