package com.pawsmedic.desktop.veterinary.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pawsmedic.desktop.veterinary.model.PatientRow
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.ClinicalEmerald
import com.pawsmedic.desktop.veterinary.ui.theme.TextPrimary
import com.pawsmedic.desktop.veterinary.ui.theme.TextSecondary
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class PetUpdatePayload(
    val name: String,
    val species: String,
    val breed: String,
    @SerialName("sex") val gender: String,
    @SerialName("weight_grams") val weightGrams: Int? = null,
    @SerialName("medical_id") val medicalId: String,
    val allergies: String? = null
)

@Composable
fun EditPetDataDialog(
    initialPatient: PatientRow,
    onDismiss: () -> Unit,
    onConfirm: (PatientRow) -> Unit
) {
    var petName by remember { mutableStateOf(initialPatient.name ?: "") }
    var species by remember { mutableStateOf(initialPatient.species ?: "Canino") }
    var breed by remember { mutableStateOf(initialPatient.breed ?: "") }
    var gender by remember { mutableStateOf(initialPatient.gender ?: "Macho") }
    var weightKg by remember { mutableStateOf(initialPatient.weightGrams?.let { "${it / 1000.0}" } ?: "") }
    var medicalId by remember { mutableStateOf(initialPatient.medicalId ?: "PM-${initialPatient.id.take(4).uppercase()}") }
    var allergies by remember { mutableStateOf(initialPatient.allergies ?: "") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(520.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Editar Datos del Paciente", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color(0xFFB91C1C), fontSize = 12.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp)).padding(8.dp))
                }

                OutlinedTextField(value = petName, onValueChange = { petName = it }, label = { Text("Nombre de la Mascota") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = species, onValueChange = { species = it }, label = { Text("Especie") }) }
                    Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Raza") }) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Sexo") }) }
                    Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = weightKg, onValueChange = { weightKg = it }, label = { Text("Peso (kg)") }) }
                }

                OutlinedTextField(value = medicalId, onValueChange = { medicalId = it }, label = { Text("ID Médico (`medical_id`)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))

                OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text("Alergias / Notas de Cuidado") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar", color = TextPrimary) }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSaving = true
                                errorMessage = null
                                try {
                                    val weightGr = weightKg.toDoubleOrNull()?.let { (it * 1000).toInt() }
                                    val payload = PetUpdatePayload(
                                        name = petName.trim(),
                                        species = species.trim(),
                                        breed = breed.trim(),
                                        gender = gender.trim(),
                                        weightGrams = weightGr,
                                        medicalId = medicalId.trim(),
                                        allergies = allergies.ifBlank { null }
                                    )

                                    val updatedRow = supabase.postgrest["pets"]
                                        .update(payload) {
                                            filter { eq("id", initialPatient.id) }
                                            select()
                                        }
                                        .decodeSingle<PatientRow>()

                                    onConfirm(updatedRow)
                                    onDismiss()
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Error al actualizar la mascota."
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                    ) {
                        Text(if (isSaving) "Guardando…" else "Guardar Cambios", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
