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
import com.pawsmedic.features.pets.domain.model.Pet
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun PetDashboardScreen(
    userName: String = "Usuario",
    activePet: Pet? = null,
    onNavigateToPetsList: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetDetail: (String) -> Unit,
    onNavigateToAppointments: () -> Unit = {},
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
                    Text(text = "👤", fontSize = 24.sp)
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
        if (activePet != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PawMedicColors.Teal50)
                    .border(1.dp, PawMedicColors.Teal200, RoundedCornerShape(20.dp))
                    .clickable { onNavigateToPetDetail(activePet.id) }
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
                            Text(
                                text = if (activePet.species.lowercase().contains("gato")) "🐱" else "🐶",
                                fontSize = 32.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = activePet.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicColors.Gray900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${activePet.species} • ${activePet.breed}",
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
        } else {
            // Estado vacío cuando el usuario registrado no tiene mascotas en Supabase
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PawMedicColors.White)
                    .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(20.dp))
                    .clickable { onNavigateToAddPet() }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🐾", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aún no tienes mascotas registradas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Gray800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toca aquí para agregar tu primera mascota",
                        fontSize = 13.sp,
                        color = PawMedicColors.Teal600
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
                onClick = onNavigateToAppointments,
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
                modifier = Modifier.clickable { onNavigateToAppointments() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Estado vacío por defecto si no hay citas registradas en la BD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PawMedicColors.White)
                .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tienes citas programadas",
                fontSize = 14.sp,
                color = PawMedicColors.Gray500
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PawMedicPrimaryButton(
            text = "Agendar cita",
            onClick = onNavigateToAppointments
        )
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