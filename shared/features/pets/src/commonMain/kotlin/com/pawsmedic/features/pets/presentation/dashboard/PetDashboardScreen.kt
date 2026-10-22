package com.pawsmedic.features.pets.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.shared.core.designsystem.components.PawMedicLogoIcon
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun PetDashboardScreen(
    userName: String = "María",
    onNavigateToPetsList: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- TOP USER HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PawMedicColors.Teal100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👩🏻", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hola, $userName 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Gray900
                    )
                    Text(
                        text = "¡Tu mascota te lo agradecerá hoy!",
                        fontSize = 13.sp,
                        color = PawMedicColors.Gray500
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PawMedicColors.White)
                    .border(1.dp, PawMedicColors.Gray200, CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔔", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- ACTIVE PET BANNER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PawMedicColors.Teal50)
                .border(1.dp, PawMedicColors.Teal200, RoundedCornerShape(20.dp))
                .clickable { onNavigateToPetDetail("pet-1") }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PawMedicColors.Teal100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🐱", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Tobías",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Gray900
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Gato • Persa Mestizo • 2 años",
                            fontSize = 13.sp,
                            color = PawMedicColors.Gray600
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PawMedicColors.Teal600)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Activo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- QUICK ACCESS SECTION ---
        Text(
            text = "Acceso Rápido",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray900
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickAccessCard(
                icon = "🐾",
                title = "Mis mascotas",
                backgroundColor = Color(0xFFE6F4EA),
                onClick = onNavigateToPetsList,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            QuickAccessCard(
                icon = "📅",
                title = "Mis citas",
                backgroundColor = Color(0xFFFEF7E0),
                onClick = { },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            QuickAccessCard(
                icon = "📄",
                title = "Historial",
                backgroundColor = Color(0xFFF3E8FF),
                onClick = { },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- UPCOMING APPOINTMENTS SECTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Próximas Citas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )
            Text(
                text = "Ver todas",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Teal600,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AppointmentCard(
            petName = "Tobías (Gato)",
            doctor = "Dra. Ana Milena • Clinipet Central",
            date = "Mañana, 10:00 AM",
            isHighlight = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppointmentCard(
            petName = "Rocky (Perro)",
            doctor = "Dr. Carlos Ruiz • Hospital Canino Norte",
            date = "24 Nov, 4:30 PM",
            isHighlight = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        PawMedicPrimaryButton(
            text = "Agendar cita",
            onClick = { }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- VETERINARIES AVAILABLE ---
        Text(
            text = "Veterinarias Disponibles",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray900
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PawMedicColors.White)
                .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Clinipet Central",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Gray900
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⭐", fontSize = 14.sp)
                        Text(
                            text = "4.9",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Gray900,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Calle 100 #15-45, Bog.",
                    fontSize = 13.sp,
                    color = PawMedicColors.Gray500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Urgencias, Cirugía, Consulta",
                    fontSize = 12.sp,
                    color = PawMedicColors.Gray400
                )
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    icon: String,
    title: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 26.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = PawMedicColors.Gray800
        )
    }
}

@Composable
private fun AppointmentCard(
    petName: String,
    doctor: String,
    date: String,
    isHighlight: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PawMedicColors.White)
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PawMedicColors.Teal50),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📅", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = petName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Gray900
                )
                Text(
                    text = doctor,
                    fontSize = 12.sp,
                    color = PawMedicColors.Gray500
                )
                Text(
                    text = date,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlight) Color(0xFFD97706) else PawMedicColors.Teal600,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
