package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.ui.window.singleWindowApplication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MobileRequest(
    val id: String,
    val petNameAndBreed: String,
    val ownerName: String,
    val reason: String,
    val timeAgo: String
)

data class CalendarTimeSlot(
    val time: String,
    val petInfo: String?,
    val service: String?,
    val isSelected: Boolean = false,
    val isAvailable: Boolean = false
)

@Composable
fun AgendaScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var selectedViewMode by remember { mutableStateOf("Día") }

    val requests = remember {
        mutableStateListOf(
            MobileRequest("1", "Max (Golden Retriever)", "Sofía Martínez", "Vacunación Anual", "Recibido hace 5m"),
            MobileRequest("2", "Luna (Siamés)", "Carlos Ruiz", "Consulta por Estornudos", "Recibido hace 12m"),
            MobileRequest("3", "Rocky (Bulldog)", "Ana Gómez", "Revisión de Cirugía", "Recibido hace 1h")
        )
    }

    val timeSlots = remember {
        listOf(
            CalendarTimeSlot("09:00 AM", "Toby (Pug)", "Estudios preoperatorios"),
            CalendarTimeSlot("10:00 AM", null, null, isAvailable = true),
            CalendarTimeSlot("11:00 AM", "Coco (Maltés)", "Consulta dermatológica recurrente", isSelected = true),
            CalendarTimeSlot("12:00 PM", "Milo (Persa)", "Desparasitación y pipeta"),
            CalendarTimeSlot("01:00 PM", null, null, isAvailable = true),
            CalendarTimeSlot("02:00 PM", null, null, isAvailable = true)
        )
    }

    VetAppLayout(
        currentScreen = "Agenda",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = "Agenda y Citas Médicas"
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // COLUMNA 1: Solicitudes Móviles (0.25f)
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Solicitudes Móviles",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(DarkSlate),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${requests.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Propuestas de citas recibidas desde la app del dueño. Aprueba para agendar automáticamente.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(requests) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OffWhiteBg)
                                    .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = item.petNameAndBreed,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Dueño: ${item.ownerName}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "Motivo: ${item.reason}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = item.timeAgo,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.White)
                                                .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                                                .clickable { requests.remove(item) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Rechazar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DarkSlate)
                                                .clickable { requests.remove(item) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Aprobar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // COLUMNA 2: Horarios / Calendario (0.50f)
            Box(
                modifier = Modifier
                    .weight(0.50f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Hoy: Lunes 15 de Octubre",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        // Switcher Día | Semana
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(OffWhiteBg)
                                .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                                .padding(2.dp)
                        ) {
                            listOf("Día", "Semana").forEach { mode ->
                                val isSel = selectedViewMode == mode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSel) Color.White else Color.Transparent)
                                        .clickable { selectedViewMode = mode }
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(timeSlots) { slot ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = slot.time,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.width(75.dp)
                                )

                                if (slot.isAvailable) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text("Espacio disponible", fontSize = 12.sp, color = TextMuted)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (slot.isSelected) Color.White else OffWhiteBg)
                                            .border(
                                                width = if (slot.isSelected) 2.dp else 1.dp,
                                                color = if (slot.isSelected) DarkSlate else BorderLight,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height(24.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(DarkSlate)
                                            )
                                            Column {
                                                Text(
                                                    text = slot.petInfo ?: "",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = slot.service ?: "",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // COLUMNA 3: Detalle de Cita (0.25f)
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Detalle de Cita",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tarjeta Mascota
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(OffWhiteBg)
                            .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SelectedBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = DarkSlate,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Coco (Maltés)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Hembra · 2 años · 4.2 kg",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detalles
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column {
                            Text("MOTIVO DE CONSULTA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Consulta dermatológica recurrente por comezón y rojez en orejas y vientre.",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }

                        Column {
                            Text("DUEÑO / PROPIETARIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Elena Rostova", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Teléfono: 55-9831-2983", fontSize = 12.sp, color = TextSecondary)
                        }

                        Column {
                            Text("HORARIO RESERVADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Hoy, 11:00 AM - 11:45 AM (Aprobado)", fontSize = 12.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botones Inferiores
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSlate)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("Iniciar Consulta Médica", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Reagendar / Modificar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AgendaScreenPreview() {
    AgendaScreen(onNavigate = {})
}

fun main() = singleWindowApplication(title = "Preview - Agenda y Citas Médicas") {
    AgendaScreen(onNavigate = {})
}