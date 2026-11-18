package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.pawsmedic.desktop.veterinary.model.resolveTenantId
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.BorderLight
import com.pawsmedic.desktop.veterinary.ui.theme.ClinicalEmerald
import com.pawsmedic.desktop.veterinary.ui.theme.EmeraldGreen
import com.pawsmedic.desktop.veterinary.ui.theme.EmeraldLightBg
import com.pawsmedic.desktop.veterinary.model.OwnerProfileRow
import com.pawsmedic.desktop.veterinary.model.PatientRow
import com.pawsmedic.desktop.veterinary.ui.dialog.EditPetDataDialog
import com.pawsmedic.desktop.veterinary.ui.dialog.LinkOrRegisterPatientDialog
import com.pawsmedic.desktop.veterinary.ui.theme.SelectedBg
import com.pawsmedic.desktop.veterinary.ui.theme.SelectedText
import com.pawsmedic.desktop.veterinary.ui.theme.TextMuted
import com.pawsmedic.desktop.veterinary.ui.theme.TextPrimary
import com.pawsmedic.desktop.veterinary.ui.theme.TextSecondary
import com.pawsmedic.features.medical_records.data.datasource.SupabaseMedicalRecordDataSource
import com.pawsmedic.features.medical_records.data.repository.DefaultMedicalRecordRepository
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.model.PetMedicalRecord
import com.pawsmedic.features.medical_records.presentation.dialog.EditBackgroundDialog
import com.pawsmedic.shared.core.common.calculateAgeFromIsoDate
import com.pawsmedic.shared.core.common.formatIsoToDisplayDate
import com.pawsmedic.shared.core.model.ProfileRole
import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun ExpedientesScreen(
    userEmail: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var searchMedicalIdQuery by remember { mutableStateOf("") }
    var patientsList by remember { mutableStateOf<List<PatientRow>>(emptyList()) }
    var selectedPatient by remember { mutableStateOf<PatientRow?>(null) }
    var selectedOwner by remember { mutableStateOf<OwnerProfileRow?>(null) }
    var fullRecord by remember { mutableStateOf<PetMedicalRecord?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Antecedentes, 1: Consultas SOAP, 2: TODAS las Recetas, 3: Examen Físico, 4: Bitácora
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingRecord by remember { mutableStateOf(false) }
    var showEditBackgroundDialog by remember { mutableStateOf(false) }
    var showLinkOrRegisterDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var vetUserId by remember { mutableStateOf<String?>(null) }
    var vetUserName by remember { mutableStateOf("Dr. Veterinario") }

    val coroutineScope = rememberCoroutineScope()
    val repository = remember { DefaultMedicalRecordRepository(SupabaseMedicalRecordDataSource(supabase)) }

    fun loadFullRecord(petId: String) {
        coroutineScope.launch {
            isLoadingRecord = true
            repository.getPetMedicalRecord(petId)
                .onSuccess { rec ->
                    fullRecord = rec
                    isLoadingRecord = false
                }
                .onFailure { err ->
                    errorMessage = err.message ?: "No se pudo cargar el expediente."
                    isLoadingRecord = false
                }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val user = supabase.auth.currentSessionOrNull()?.user
                ?: error("No hay una sesión activa en Supabase.")
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", user.id) } }
                .decodeSingle<UserProfile>()
            if (profile.role != ProfileRole.VETERINARY_BUSINESS && profile.role != ProfileRole.SUPERADMIN) {
                error("Los expedientes solo están disponibles para cuentas veterinarias.")
            }
            vetUserId = user.id
            vetUserName = profile.displayName.ifBlank { "Dr. Veterinario" }

            val pets = supabase.postgrest["pets"]
                .select()
                .decodeList<PatientRow>()
            patientsList = pets
            if (pets.isNotEmpty()) {
                selectedPatient = pets.first()
                loadFullRecord(pets.first().id)
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "No se pudieron cargar los expedientes desde Supabase."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(selectedPatient) {
        val p = selectedPatient
        if (p != null) {
            loadFullRecord(p.id)
            val ownerId = p.ownerId
            if (!ownerId.isNullOrBlank()) {
                runCatching {
                    supabase.postgrest["profiles"]
                        .select { filter { eq("id", ownerId) } }
                        .decodeSingleOrNull<OwnerProfileRow>()
                }.onSuccess { owner ->
                    selectedOwner = owner
                }
            } else {
                selectedOwner = null
            }
        }
    }

    if (showLinkOrRegisterDialog) {
        LinkOrRegisterPatientDialog(
            onDismiss = { showLinkOrRegisterDialog = false },
            onPatientSelected = { newPatient ->
                if (patientsList.none { it.id == newPatient.id }) {
                    patientsList = listOf(newPatient) + patientsList
                }
                selectedPatient = newPatient
                loadFullRecord(newPatient.id)
            }
        )
    }

    if (showEditPetDialog && selectedPatient != null) {
        EditPetDataDialog(
            initialPatient = selectedPatient!!,
            onDismiss = { showEditPetDialog = false },
            onConfirm = { updatedPet ->
                patientsList = patientsList.map { if (it.id == updatedPet.id) updatedPet else it }
                selectedPatient = updatedPet
                loadFullRecord(updatedPet.id)
            }
        )
    }

    if (showEditBackgroundDialog && fullRecord != null) {
        EditBackgroundDialog(
            initialBackground = fullRecord!!.background,
            onDismiss = { showEditBackgroundDialog = false },
            onConfirm = { updatedBg ->
                coroutineScope.launch {
                    repository.updateBackground(updatedBg.copy(updatedBy = vetUserId))
                        .onSuccess {
                            showEditBackgroundDialog = false
                            selectedPatient?.let { loadFullRecord(it.id) }
                        }
                        .onFailure { err ->
                            errorMessage = err.message ?: "No se pudieron guardar los antecedentes en Supabase."
                            showEditBackgroundDialog = false
                        }
                }
            }
        )
    }

    VetAppLayout(
        currentScreen = "Expedientes",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = "Expedientes Clínicos Digitales",
        userEmail = userEmail
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PANEL IZQUIERDO: Directorio de Pacientes por medical_id (0.32f)
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // BOTÓN: VINCULAR / REGISTRAR PACIENTE POR MEDICAL_ID
                    Button(
                        onClick = { showLinkOrRegisterDialog = true },
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("➕ Vincular por ID Médico (`medical_id`)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // BÚSQUEDA POR MEDICAL_ID O NOMBRE
                    OutlinedTextField(
                        value = searchMedicalIdQuery,
                        onValueChange = { searchMedicalIdQuery = it },
                        placeholder = { Text("Buscar por ID Médico (PM-XXXX) o Nombre", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val filtered = patientsList.filter { p ->
                        val mId = p.medicalId ?: "PM-${p.id.take(4).uppercase()}"
                        val pName = p.name ?: ""
                        searchMedicalIdQuery.isBlank() ||
                            mId.contains(searchMedicalIdQuery, ignoreCase = true) ||
                            pName.contains(searchMedicalIdQuery, ignoreCase = true) ||
                            p.id.contains(searchMedicalIdQuery, ignoreCase = true)
                    }

                    if (isLoading) {
                        Text("Cargando pacientes…", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                    } else if (filtered.isEmpty()) {
                        Text("No se encontraron expedientes.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtered) { p ->
                                val isSelected = selectedPatient?.id == p.id
                                val medIdStr = p.medicalId ?: "PM-${p.id.take(4).uppercase()}"
                                val nameStr = p.name ?: "Mascota"
                                val speciesStr = p.species ?: "Especie"
                                val breedStr = p.breed ?: "Mestizo"

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) SelectedBg else Color.White)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) EmeraldGreen else BorderLight,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedPatient = p }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldLightBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Pets,
                                                contentDescription = null,
                                                tint = ClinicalEmerald,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = nameStr,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(ClinicalEmerald)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = medIdStr,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "$speciesStr • $breedStr",
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

            // PANEL DERECHO: Expediente Clínico Completo (0.68f)
            Box(
                modifier = Modifier
                    .weight(0.68f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    val p = selectedPatient
                    val rec = fullRecord

                    if (p != null) {
                        val medId = p.medicalId ?: "PM-${p.id.take(4).uppercase()}"
                        val pName = p.name ?: "Mascota"
                        val pSpecies = p.species ?: "Especie"
                        val pBreed = p.breed ?: "Mestizo"
                        val computedAge = calculateAgeFromIsoDate(p.dateOfBirth) ?: "Edad no especificada"
                        val ownerNameStr = listOfNotNull(selectedOwner?.firstName, selectedOwner?.lastName).joinToString(" ").ifBlank {
                            selectedOwner?.displayName ?: "Propietario Registrado"
                        }

                        // HEADER DEL EXPEDIENTE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldLightBg)
                                        .border(2.dp, ClinicalEmerald, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(pName.take(2).uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelectedText)
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(pName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(ClinicalEmerald)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ID Médico: $medId", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                    Text("$pSpecies • $pBreed • Edad: $computedAge", fontSize = 12.sp, color = TextSecondary)
                                    Text("Propietario: $ownerNameStr • Tel: ${selectedOwner?.phone ?: "N/A"}", fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { showEditPetDialog = true },
                                    modifier = Modifier.height(34.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Editar Paciente", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                }

                                OutlinedButton(
                                    onClick = { showEditBackgroundDialog = true },
                                    modifier = Modifier.height(34.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Editar Antecedentes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                }

                                Button(
                                    onClick = { onNavigate("Consultas") },
                                    modifier = Modifier.height(34.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Ir a Consultas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(errorMessage!!, color = Color(0xFFB91C1C), fontSize = 11.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp)).padding(6.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // PESTAÑAS DEL EXPEDIENTE
                        val tabs = listOf(
                            "Antecedentes Médicos",
                            "Consultas (SOAP)",
                            "TODAS las Recetas",
                            "Examen Físico & 12 Sistemas",
                            "Estudios & Adjuntos",
                            "Bitácora Auditoría"
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tabs.forEachIndexed { idx, title ->
                                val isSel = selectedTab == idx
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) SelectedBg else Color.Transparent)
                                        .border(1.dp, if (isSel) BorderLight else Color.Transparent, RoundedCornerShape(6.dp))
                                        .clickable { selectedTab = idx }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) SelectedText else TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isLoadingRecord) {
                            Text("Cargando expediente de $pName…", fontSize = 13.sp, color = TextSecondary)
                        } else if (rec != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                when (selectedTab) {
                                    0 -> { // ANTECEDENTES
                                        val bg = rec.background
                                        Text("ANTECEDENTES CLÍNICOS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                        Text("Alimentación: ${bg.diet.ifBlank { "Croquetas comerciales" }}", fontSize = 13.sp, color = TextPrimary)
                                        Text("Alergias Conocidas: ${bg.allergies.ifBlank { p.allergies ?: "Ninguna reportada" }}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                        Text("Última Desparasitación: ${bg.lastDewormingDate ?: "No registrada"} (${bg.dewormingProduct})", fontSize = 13.sp, color = TextPrimary)
                                        Text("Enfermedades Anteriores: ${bg.pastDiseases.ifBlank { "Ninguna" }}", fontSize = 13.sp, color = TextPrimary)
                                        Text("Cirugías Anteriores: ${bg.pastSurgeries.ifBlank { "Ninguna" }}", fontSize = 13.sp, color = TextPrimary)
                                        Text("Antecedentes Familiares: ${bg.familyHistory.ifBlank { "Sin datos" }}", fontSize = 13.sp, color = TextPrimary)
                                        Text("Hábitat: ${bg.habitat.ifBlank { "Interior sin acceso a calle" }}", fontSize = 13.sp, color = TextPrimary)
                                    }

                                    1 -> { // CONSULTAS SOAP
                                        Text("CONSULTAS CLÍNICAS (SOAP)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                        if (rec.consultations.isEmpty()) {
                                            Text("Sin consultas clínicas registradas previamente.", fontSize = 13.sp, color = TextMuted)
                                        } else {
                                            rec.consultations.forEach { c ->
                                                Column(modifier = Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text("Fecha: ${c.occurredAt} • Vet: ${c.veterinarianName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                        Text("Estado: ${c.status}", fontSize = 11.sp, color = TextMuted)
                                                    }
                                                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))
                                                    Text("Motivo: ${c.reason}", fontSize = 13.sp, color = TextPrimary)
                                                    if (c.anamnesis.isNotBlank()) Text("Anamnesis: ${c.anamnesis}", fontSize = 12.sp, color = TextSecondary)
                                                    if (c.definitiveDiagnosis.isNotBlank()) Text("Diagnóstico: ${c.definitiveDiagnosis}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                                    if (c.treatmentPlan.isNotBlank()) Text("Tratamiento: ${c.treatmentPlan}", fontSize = 12.sp, color = TextPrimary)
                                                }
                                            }
                                        }
                                    }

                                    2 -> { // TODAS LAS RECETAS
                                        Text("RECETAS MÉDICAS Y FÁRMACOS (HISTORIAL COMPLETO)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                        if (rec.prescriptions.isEmpty()) {
                                            Text("Sin recetas registradas para este paciente.", fontSize = 13.sp, color = TextMuted)
                                        } else {
                                            rec.prescriptions.forEach { pres ->
                                                Column(modifier = Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text(pres.drugName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(if (pres.isActive) Color(0xFFE6F4EA) else com.pawsmedic.desktop.veterinary.ui.theme.OffWhiteBg)
                                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = if (pres.isActive) "🟢 ACTIVA (Vence: ${pres.endDate ?: "N/A"})" else "⚪ VENCIDA (Venció: ${pres.endDate ?: "N/A"})",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (pres.isActive) EmeraldGreen else TextMuted
                                                            )
                                                        }
                                                    }
                                                    Text("Dosis: ${pres.dosage} ${pres.unit} • Vía: ${pres.route} • Frecuencia: ${pres.frequency} • Duración: ${pres.duration}", fontSize = 12.sp, color = TextSecondary)
                                                    Text("Emita por: ${pres.prescribingVetName.ifBlank { "Dr. Veterinario" }}", fontSize = 11.sp, color = TextMuted)
                                                    if (pres.notes.isNotBlank()) Text("Instrucciones: ${pres.notes}", fontSize = 12.sp, color = TextPrimary)
                                                }
                                            }
                                        }
                                    }

                                    3 -> { // EXAMEN FISICO
                                        val latest = rec.consultations.firstOrNull()
                                        if (latest == null) {
                                            Text("Sin datos de examen físico recientes.", fontSize = 13.sp, color = TextMuted)
                                        } else {
                                            val v = latest.vitalSigns
                                            val s = latest.systemsExploration
                                            Text("CONSTANTES FISIOLÓGICAS RECIENTES", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                            Text("Temp: ${v.temperatureCelsius ?: 38.5} °C • FC: ${v.heartRateBpm ?: 120} bpm • FR: ${v.respiratoryRateRpm ?: 24} rpm • Peso: ${(v.weightGrams ?: 4200) / 1000.0} kg", fontSize = 13.sp, color = TextPrimary)
                                            Text("TLLC: ${v.tllcSeconds} • Hidratación: ${v.hydrationStatus} • Condición: ${v.bodyConditionScore}", fontSize = 12.sp, color = TextSecondary)

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("EXPLORACIÓN DE 12 SISTEMAS ORGÁNICOS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                            Text("Mucosas: ${s.mucosas}", fontSize = 12.sp, color = TextPrimary)
                                            Text("Ojos: ${s.ojos} • Oídos: ${s.oidos}", fontSize = 12.sp, color = TextPrimary)
                                            Text("Piel y Anexos: ${s.pielAnexos}", fontSize = 12.sp, color = TextPrimary)
                                            Text("Sistema Respiratorio: ${s.sistemaRespiratorio}", fontSize = 12.sp, color = TextPrimary)
                                            Text("Sistema Digestivo: ${s.sistemaDigestivo}", fontSize = 12.sp, color = TextPrimary)
                                        }
                                    }

                                    4 -> { // ESTUDIOS
                                        Text("ESTUDIOS CLÍNICOS E IMÁGENES", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                        if (rec.studies.isEmpty()) {
                                            Text("Sin estudios o imágenes adjuntas.", fontSize = 13.sp, color = TextMuted)
                                        } else {
                                            rec.studies.forEach { st ->
                                                Text("Estudio: ${st.name} (${st.studyType}) - Fecha: ${st.requestedAt}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                if (st.resultsNotes.isNotBlank()) Text("Resultados: ${st.resultsNotes}", fontSize = 12.sp, color = TextSecondary)
                                            }
                                        }
                                    }

                                    5 -> { // AUDITORIA
                                        Text("BITÁCORA DE AUDITORÍA Y TRAZABILIDAD", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                        if (rec.auditLogs.isEmpty()) {
                                            Text("Sin registros de auditoría para este expediente.", fontSize = 13.sp, color = TextMuted)
                                        } else {
                                            rec.auditLogs.forEach { log ->
                                                Text("${log.timestamp} • ${log.action} por ${log.actorName} (${log.actorRole}): ${log.details}", fontSize = 12.sp, color = TextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Selecciona un paciente a la izquierda para ver su expediente.", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
