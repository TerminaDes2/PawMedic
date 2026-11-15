package com.pawsmedic.desktop.veterinary.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pawsmedic.desktop.veterinary.model.NewPetDesktopRecord
import com.pawsmedic.desktop.veterinary.model.PatientRow
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.BorderLight
import com.pawsmedic.desktop.veterinary.ui.theme.ClinicalEmerald
import com.pawsmedic.desktop.veterinary.ui.theme.EmeraldLightBg
import com.pawsmedic.desktop.veterinary.ui.theme.SelectedBg
import com.pawsmedic.desktop.veterinary.ui.theme.TextMuted
import com.pawsmedic.desktop.veterinary.ui.theme.TextPrimary
import com.pawsmedic.desktop.veterinary.ui.theme.TextSecondary
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun LinkOrRegisterPatientDialog(
    onDismiss: () -> Unit,
    onPatientSelected: (PatientRow) -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Buscar por medical_id, 1: Registrar nueva mascota
    var searchMedicalId by remember { mutableStateOf("") }
    var searchedPatient by remember { mutableStateOf<PatientRow?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Formulario Nueva Mascota
    var petName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("Canino") }
    var breed by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Macho") }
    var weightKg by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var generatedMedicalId by remember { mutableStateOf("PM-${UUID.randomUUID().toString().take(6).uppercase()}") }

    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(580.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EmeraldLightBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Pets, contentDescription = null, tint = ClinicalEmerald, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ingresar / Vincular Paciente",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // TABS: BÚSQUEDA vs REGISTRO NUEVO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1. Buscar por ID Médico (medical_id)", "2. Registrar Nueva Mascota").forEachIndexed { idx, label ->
                        val isSel = activeTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) ClinicalEmerald else SelectedBg)
                                .border(1.dp, if (isSel) ClinicalEmerald else BorderLight, RoundedCornerShape(8.dp))
                                .clickable { activeTab = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else TextSecondary
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFB91C1C),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    )
                }

                when (activeTab) {
                    0 -> { // BUSCAR POR MEDICAL_ID
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Ingresa el ID Médico (`medical_id`) o el Folio de la Mascota:", fontSize = 13.sp, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = searchMedicalId,
                                    onValueChange = { searchMedicalId = it },
                                    placeholder = { Text("Ej. PM-8942-A o ID UUID") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        if (searchMedicalId.isBlank()) return@Button
                                        coroutineScope.launch {
                                            isSearching = true
                                            errorMessage = null
                                            searchedPatient = null
                                            try {
                                                val queryStr = searchMedicalId.trim()
                                                val results = supabase.postgrest["pets"]
                                                    .select {
                                                        filter {
                                                            or {
                                                                eq("medical_id", queryStr)
                                                                eq("id", queryStr)
                                                            }
                                                        }
                                                    }
                                                    .decodeList<PatientRow>()
                                                if (results.isEmpty()) {
                                                    errorMessage = "No se encontró ninguna mascota con el ID Médico '$queryStr'."
                                                } else {
                                                    searchedPatient = results.first()
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = e.message ?: "Error al buscar mascota en Supabase."
                                            } finally {
                                                isSearching = false
                                            }
                                        }
                                    },
                                    enabled = !isSearching,
                                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isSearching) "Buscando…" else "Buscar", color = Color.White)
                                }
                            }

                            if (searchedPatient != null) {
                                val p = searchedPatient!!
                                val medIdStr = p.medicalId ?: "PM-${p.id.take(4).uppercase()}"
                                val nameStr = p.nombre ?: p.name ?: "Mascota"
                                val speciesStr = p.especie ?: p.species ?: "Especie"
                                val breedStr = p.raza ?: p.breed ?: "Mestizo"

                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = EmeraldLightBg),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ClinicalEmerald)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("¡PACIENTE ENCONTRADO!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Nombre: $nameStr", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("ID Médico: $medIdStr • Especie: $speciesStr • Raza: $breedStr", fontSize = 12.sp, color = TextSecondary)

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                onPatientSelected(p)
                                                onDismiss()
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                                        ) {
                                            Text("✅ Vincular y Abrir Paciente", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // REGISTRAR NUEVA MASCOTA MANUALMENTE
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = petName,
                                onValueChange = { petName = it },
                                label = { Text("Nombre de la Mascota (*Requerido)") },
                                placeholder = { Text("Ej. Max, Tobías") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(value = species, onValueChange = { species = it }, label = { Text("Especie (Canino, Felino)") }, singleLine = true)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Raza (ej. Poodle)") }, singleLine = true)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Sexo (Macho / Hembra)") }, singleLine = true)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(value = weightKg, onValueChange = { weightKg = it }, label = { Text("Peso aprox (kg)") }, singleLine = true)
                                }
                            }

                            OutlinedTextField(
                                value = generatedMedicalId,
                                onValueChange = { generatedMedicalId = it },
                                label = { Text("ID Médico Asignado (`medical_id`)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = allergies,
                                onValueChange = { allergies = it },
                                label = { Text("Notas de Cuidado / Alergias") },
                                placeholder = { Text("Alergias o tratamientos continuos") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (petName.isBlank()) {
                                        errorMessage = "El nombre de la mascota es obligatorio."
                                        return@Button
                                    }

                                    coroutineScope.launch {
                                        isSaving = true
                                        errorMessage = null
                                        try {
                                            val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""
                                            val weightGr = weightKg.toDoubleOrNull()?.let { (it * 1000).toInt() }
                                            val newRec = NewPetDesktopRecord(
                                                ownerId = currentUserId,
                                                name = petName.trim(),
                                                species = species.ifBlank { "Canino" },
                                                breed = breed.ifBlank { "Mestizo" },
                                                gender = gender.ifBlank { "Macho" },
                                                weightGrams = weightGr,
                                                medicalId = generatedMedicalId.trim(),
                                                allergies = allergies.ifBlank { null }
                                            )

                                            val inserted = supabase.postgrest["pets"]
                                                .insert(newRec) { select() }
                                                .decodeSingle<PatientRow>()

                                            onPatientSelected(inserted)
                                            onDismiss()
                                        } catch (e: Exception) {
                                            errorMessage = e.message ?: "Error al registrar nueva mascota en Supabase."
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                },
                                enabled = !isSaving,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                            ) {
                                Text(if (isSaving) "Guardando…" else "💾 Guardar y Abrir Paciente", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
