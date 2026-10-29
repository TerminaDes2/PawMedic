package com.pawsmedic.features.pets.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.pets.domain.model.Pet
import com.pawsmedic.features.pets.presentation.components.PetAvatarImage
import com.pawsmedic.shared.core.common.rememberPhotoPicker
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import io.github.jan.supabase.SupabaseClient

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    supabaseClient: SupabaseClient,
    pets: List<Pet>,
    appointmentsCount: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val profile = state.profile
    val photoPicker = rememberPhotoPicker(
        supabaseClient = supabaseClient,
        bucketName = "profiles",
        folderName = viewModel.userId,
        onPhotoPicked = viewModel::updateFoto_url,
        onUploadError = viewModel::onError
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(title = "Mi perfil", onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .clickable { photoPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    PetAvatarImage(
                        foto_url = profile?.foto_url,
                        species = null,
                        size = 112.dp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cambiar foto",
                    color = PawMedicColors.Teal600,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = listOf(profile?.nombre.orEmpty(), profile?.apellidos.orEmpty())
                        .joinToString(" ")
                        .trim()
                        .ifBlank { "Usuario" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Gray900,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        ProfileInfoRow("Email", profile?.email.orEmpty())
        ProfileInfoRow("Número", profile?.numTel ?: "No registrado")
        ProfileInfoRow("Cuenta creada", profile?.createdAt?.takeIf { it.isNotBlank() }?.substringBefore("T") ?: "No disponible")
        ProfileInfoRow("Estado", "Manzanillo, Colima")

        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStat("Mascotas", pets.size, Modifier.weight(1f))
            ProfileStat("Citas", appointmentsCount, Modifier.weight(1f))
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(state.errorMessage.orEmpty(), color = PawMedicColors.Error, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(label, fontSize = 12.sp, color = PawMedicColors.Gray500)
        Text(value.ifBlank { "No disponible" }, fontSize = 16.sp, color = PawMedicColors.Gray900)
    }
}

@Composable
private fun ProfileStat(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PawMedicColors.Teal50)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
        Text(label, fontSize = 13.sp, color = PawMedicColors.Gray700)
    }
}
