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

data class Patient(
    val id: String,
    val name: String,
    val breed: String,
    val species: String,
    val age: String,
    val weight: String,
    val owner: String,
    val phone: String,
    val lastVisit: String,
    val allergies: String?
)

@Composable
fun ExpedientesScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("Todos") }
    var selectedTab by remember { mutableStateOf(0) }

    val patients = remember {
        listOf(
            Patient("1", "Coco", "Maltés", "Canino", "2 años", "4.2 kg", "Elena Rostova", "55-9831-2983", "Hoy", "Penicilina"),
            Patient("2", "Thor", "Pastor Alemán", "Canino", "4 años", "32.5 kg", "Roberto Fernández", "55-9876-5432", "28/09/2026", "Derivados de Sulfa"),
            Patient("3", "Luna", "Siamés", "Felino", "2 años 8 meses", "4.1 kg", "Ana Silva", "55-1234-9876", "20/09/2026", null),
            Patient("4", "Max", "Golden Retriever", "Canino", "5 años", "30.0 kg", "Carlos Mendoza", "55-5555-1234", "15/09/2026", null),
            Patient("5", "Miso", "Mestizo", "Felino", "1 año 3 meses", "3.8 kg", "Elena Torres", "55-8888-2222", "28/08/2026", null)
        )
    }

    var selectedPatient by remember { mutableStateOf(patients[0]) }

    VetAppLayout(
        currentScreen = "Expedientes",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = "Expedientes Clínicos"
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PANEL IZQUIERDO: Directorio de Pacientes (0.32f)
            Box(
                modifier = Modifier
                    .weight(0.32f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Pacientes Registrados",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chips de Categoría
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Todos", "Caninos", "Felinos").forEach { cat ->
                            val isSel = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) DarkSlate else OffWhiteBg)
                                    .border(1.dp, if (isSel) DarkSlate else BorderLight, RoundedCornerShape(6.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val filtered = patients.filter {
                        selectedCategory == "Todos" ||
                                (selectedCategory == "Caninos" && it.species == "Canino") ||
                                (selectedCategory == "Felinos" && it.species == "Felino")
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered) { p ->
                            val isSelected = p.id == selectedPatient.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) SelectedBg else Color.White)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) DarkSlate else BorderLight,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedPatient = p }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(OffWhiteBg)
                                            .border(1.dp, BorderLight, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (p.species == "Canino") Icons.Default.Pets else Icons.Default.CatchingPokemon,
                                            contentDescription = null,
                                            tint = DarkSlate,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = p.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = p.species,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextMuted
                                            )
                                        }
                                        Text(
                                            text = "${p.breed} · ${p.age}",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "Dueño: ${p.owner}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // PANEL DERECHO: Ficha Médica e Historial SOAP (0.68f)
            Box(
                modifier = Modifier
                    .weight(0.68f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Ficha Médica
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(OffWhiteBg)
                                    .border(1.dp, BorderLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedPatient.name.take(2).uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Column {
                                Text(
                                    text = "${selectedPatient.name} (${selectedPatient.breed})",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Especie: ${selectedPatient.species} · Edad: ${selectedPatient.age} · Peso: ${selectedPatient.weight}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Propietario: ${selectedPatient.owner} · Tel: ${selectedPatient.phone}",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSlate)
                                .clickable { }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Nueva Consulta", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    if (selectedPatient.allergies != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEE2E2))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Alergias Conocidas: ${selectedPatient.allergies}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pestañas
                    val tabs = listOf("SOAP / Consultas", "Vacunación", "Desparasitación", "Exámenes")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabs.forEachIndexed { idx, title ->
                            val isSel = selectedTab == idx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SelectedBg else Color.Transparent)
                                    .border(1.dp, if (isSel) BorderLight else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedTab = idx }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contenido SOAP
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(OffWhiteBg)
                            .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fecha: Hoy (11:00 AM) · Dr. Valdez", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Estado: Finalizada", fontSize = 12.sp, color = TextMuted)
                            }

                            HorizontalDivider(color = BorderLight)

                            Text("S (Subjetivo): El dueño consulta por enrojecimiento y prurito recurrente en orejas y vientre.", fontSize = 13.sp, color = TextPrimary)
                            Text("O (Objetivo): Lesiones eritematosas moderadas en pabellón auricular y zona ventral. Sin secreción purulenta.", fontSize = 13.sp, color = TextPrimary)
                            Text("A (Evaluación): Dermatitis atópica / Alergia por contacto.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("P (Plan & Tratamiento): Limpieza auricular diaria con solución antiséptica + antihistamínico vía oral 5 días.", fontSize = 13.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ExpedientesScreenPreview() {
    ExpedientesScreen(onNavigate = {})
}

fun main() = singleWindowApplication(title = "Preview - Expedientes Clínicos") {
    ExpedientesScreen(onNavigate = {})
}
