package com.pawsmedic.features.medical_records.presentation.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun EditBackgroundDialog(
    initialBackground: PetBackground,
    onDismiss: () -> Unit,
    onConfirm: (PetBackground) -> Unit
) {
    var dewormingDate by remember { mutableStateOf(initialBackground.lastDewormingDate ?: "") }
    var dewormingProduct by remember { mutableStateOf(initialBackground.dewormingProduct) }
    var diet by remember { mutableStateOf(initialBackground.diet) }
    var allergies by remember { mutableStateOf(initialBackground.allergies) }
    var pastDiseases by remember { mutableStateOf(initialBackground.pastDiseases) }
    var pastSurgeries by remember { mutableStateOf(initialBackground.pastSurgeries) }
    var familyHistory by remember { mutableStateOf(initialBackground.familyHistory) }
    var reproductiveStatus by remember { mutableStateOf(initialBackground.reproductiveStatus) }
    var habitat by remember { mutableStateOf(initialBackground.habitat) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val updated = initialBackground.copy(
                        lastDewormingDate = dewormingDate.ifBlank { null },
                        dewormingProduct = dewormingProduct,
                        diet = diet,
                        allergies = allergies,
                        pastDiseases = pastDiseases,
                        pastSurgeries = pastSurgeries,
                        familyHistory = familyHistory,
                        reproductiveStatus = reproductiveStatus,
                        habitat = habitat
                    )
                    onConfirm(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PawMedicColors.Teal600)
            ) {
                Text("Guardar Cambios", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PawMedicColors.Gray700)
            }
        },
        title = {
            Text(
                text = "Actualizar Antecedentes Médicos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Alimentación Actual", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                OutlinedTextField(
                    value = diet,
                    onValueChange = { diet = it },
                    placeholder = { Text("Ej. Croquetas Hills 200g/día + alimento húmedo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Alergias Conocidas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    placeholder = { Text("Ej. Penicilina, Polen, Alimentos con pollo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Última Desparasitación", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                        OutlinedTextField(
                            value = dewormingDate,
                            onValueChange = { dewormingDate = it },
                            placeholder = { Text("AAAA-MM-DD") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Producto Antiparasitario", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                        OutlinedTextField(
                            value = dewormingProduct,
                            onValueChange = { dewormingProduct = it },
                            placeholder = { Text("Ej. Bravecto") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Text("Enfermedades Anteriores", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                OutlinedTextField(
                    value = pastDiseases,
                    onValueChange = { pastDiseases = it },
                    placeholder = { Text("Ej. Gastroenteritis en 2022") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Cirugías Anteriores", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                OutlinedTextField(
                    value = pastSurgeries,
                    onValueChange = { pastSurgeries = it },
                    placeholder = { Text("Ej. Orquiectomía / Castración") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Antecedentes Familiares", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                OutlinedTextField(
                    value = familyHistory,
                    onValueChange = { familyHistory = it },
                    placeholder = { Text("Ej. Padre con displasia") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Estado Reproductivo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                OutlinedTextField(
                    value = reproductiveStatus,
                    onValueChange = { reproductiveStatus = it },
                    placeholder = { Text("Ej. Esterilizado / Intacto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Hábitat y Condiciones de Vida", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                OutlinedTextField(
                    value = habitat,
                    onValueChange = { habitat = it },
                    placeholder = { Text("Ej. Interior de departamento sin acceso a calle") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    )
}
