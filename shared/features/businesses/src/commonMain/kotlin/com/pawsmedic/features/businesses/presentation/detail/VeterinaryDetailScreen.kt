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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.businesses.presentation.VeterinaryDetailViewModel
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun VeterinaryDetailScreen(
    viewModel: VeterinaryDetailViewModel,
    onBackClick: () -> Unit,
    onStartBooking: (String) -> Unit,
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
                Text(text = "🏥", fontSize = 72.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- TITLE ---
            Text(
                text = clinic.name,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Teal600
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- RATING CARD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PawMedicColors.White)
                    .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "Calificación",
                        fontSize = 12.sp,
                        color = PawMedicColors.Gray400
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⭐", fontSize = 16.sp)
                        Text(
                            text = "${clinic.rating} (${clinic.reviewCount})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Gray900,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }

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
                Text(text = "📍", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = clinic.address ?: "Calle 100 #15-45, Bogotá, Colombia",
                    fontSize = 14.sp,
                    color = PawMedicColors.Gray700
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📞", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = clinic.phone ?: "+57 (601) 555-0199",
                    fontSize = 14.sp,
                    color = PawMedicColors.Gray700
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = clinic.description ?: "Ofrecemos atención integral de la más alta calidad...",
                fontSize = 14.sp,
                color = PawMedicColors.Gray600,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- HORARIOS DE ATENCION CARD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFEF9C3))
                    .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🕒", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Horarios de Atención",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Gray900
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    clinic.hours.forEach { hour ->
                        Text(
                            text = "• $hour",
                            fontSize = 13.sp,
                            color = PawMedicColors.Gray800,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ESPECIALIDADES DISPONIBLES ---
            Text(
                text = "Especialidades Disponibles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                clinic.specialties.take(6).chunked(3).forEach { rowSpecs ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowSpecs.forEach { spec ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(PawMedicColors.White)
                                    .border(1.dp, PawMedicColors.Gray300, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = spec,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PawMedicColors.Gray800
                                )
                            }
                        }
                    }
                }
            }

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
