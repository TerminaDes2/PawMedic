package com.pawsmedic.features.businesses.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.businesses.presentation.VeterinaryDetailViewModel
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import io.github.jan.supabase.SupabaseClient

@Composable
fun VeterinaryDetailScreen(
    viewModel: VeterinaryDetailViewModel,
    supabaseClient: SupabaseClient? = null,
    onBackClick: () -> Unit,
    onStartBooking: (String) -> Unit,
    onOpenProductCatalog: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val clinic = uiState.clinic
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = "Perfil Veterinaria",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Text("Cargando perfil de la clínica…", color = PawMedicColors.Gray600)
        }
        uiState.errorMessage?.let { message ->
            Text(
                text = "No se pudo cargar el perfil: $message",
                fontSize = 14.sp,
                color = PawMedicColors.Error
            )
            Spacer(modifier = Modifier.height(8.dp))
            PawMedicPrimaryButton(text = "Reintentar", onClick = viewModel::loadClinic)
        }
        if (clinic == null && !uiState.isLoading && uiState.errorMessage == null) {
            Text("La clínica no está disponible en el directorio activo.", color = PawMedicColors.Gray600)
        }

        if (clinic != null) {
            // --- GALLERY IMAGE BANNER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PawMedicColors.Teal50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = PawMedicColors.Teal600,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- TITLE ---
            Text(
                text = clinic.name,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Teal600
            )


            Spacer(modifier = Modifier.height(20.dp))

            // --- INFORMACION GENERAL ---
            Text(
                text = "Información General",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PawMedicColors.Teal600,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = clinic.address?.takeIf(String::isNotBlank) ?: "Dirección no registrada",
                    fontSize = 14.sp,
                    color = PawMedicColors.Gray700
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = PawMedicColors.Teal600,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = clinic.phone?.takeIf(String::isNotBlank) ?: "Teléfono no registrado",
                    fontSize = 14.sp,
                    color = PawMedicColors.Gray700
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            clinic.description?.takeIf(String::isNotBlank)?.let { description ->
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = PawMedicColors.Gray600,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OpeningHoursCard(
                schedules = clinic.schedules
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SERVICIOS DISPONIBLES ---
            Text(
                text = "Servicios disponibles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (supabaseClient != null) {
                ShowServices(
                    tenantId = clinic.id,
                    supabaseClient = supabaseClient
                )
            } else {
                Text(
                    text = "No se pudo conectar con el catálogo de servicios.",
                    color = PawMedicColors.Gray600
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            PawMedicPrimaryButton(
                text = "Ver catálogo de productos",
                onClick = { onOpenProductCatalog(clinic.id, clinic.name) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- AGENDAR CITA BUTTON ---
            PawMedicPrimaryButton(
                text = "Agendar cita",
                onClick = { onStartBooking(clinic.id) }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
