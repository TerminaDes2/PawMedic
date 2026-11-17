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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.pawsmedic.desktop.veterinary.model.OwnerProfileRow
import com.pawsmedic.desktop.veterinary.model.PatientRow
import com.pawsmedic.desktop.veterinary.model.resolveTenantId
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.dialog.EditPetDataDialog
import com.pawsmedic.desktop.veterinary.ui.dialog.LinkOrRegisterPatientDialog
import com.pawsmedic.desktop.veterinary.ui.theme.BorderLight
import com.pawsmedic.desktop.veterinary.ui.theme.ClinicalEmerald
import com.pawsmedic.desktop.veterinary.ui.theme.EmeraldGreen
import com.pawsmedic.desktop.veterinary.ui.theme.EmeraldLightBg
import com.pawsmedic.desktop.veterinary.ui.theme.SelectedBg
import com.pawsmedic.desktop.veterinary.ui.theme.SelectedText
import com.pawsmedic.desktop.veterinary.ui.theme.TextMuted
import com.pawsmedic.desktop.veterinary.ui.theme.TextPrimary
import com.pawsmedic.desktop.veterinary.ui.theme.TextSecondary
import com.pawsmedic.features.medical_records.data.datasource.SupabaseMedicalRecordDataSource
import com.pawsmedic.features.medical_records.data.repository.DefaultMedicalRecordRepository
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.model.SystemsExploration
import com.pawsmedic.features.medical_records.domain.model.VitalSigns
import com.pawsmedic.shared.core.common.convertMillisToIsoDateString
import com.pawsmedic.shared.core.model.ProfileRole
import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun ConsultasScreen(
    userEmail: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var searchMedicalIdQuery by remember { mutableStateOf("") }
    var patientsList by remember { mutableStateOf<List<PatientRow>>(emptyList()) }
    var selectedPatient by remember { mutableStateOf<PatientRow?>(null) }
    var selectedOwner by remember { mutableStateOf<OwnerProfileRow?>(null) }
    var tenantId by remember { mutableStateOf<String?>(null) }
    var vetUserId by remember { mutableStateOf<String?>(null) }
    var vetUserName by remember { mutableStateOf("Dr. Veterinario") }
    var isLoadingPatients by remember { mutableStateOf(true) }
    var isSavingConsultation by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showLinkOrRegisterDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }

    // FORMULARIO DE CONSULTA
    var formSection by remember { mutableStateOf(0) } // 0: Anamnesis, 1: Examen, 2: Diagnósticos, 3: Receta & Estudios
    var reason by remember { mutableStateOf("") }
    var anamnesis by remember { mutableStateOf("") }

    // Constantes Fisiológicas
    var tempInput by remember { mutableStateOf("38.5") }
    var fcInput by remember { mutableStateOf("120") }
    var frInput by remember { mutableStateOf("24") }
    var weightInput by remember { mutableStateOf("4.2") }
    var pulseInput by remember { mutableStateOf("Fuerte y regular") }
    var tllcInput by remember { mutableStateOf("< 2 seg") }
    var hydrationInput by remember { mutableStateOf("Normal (0-5%)") }
    var bcsInput by remember { mutableStateOf("3/5 (Ideal)") }
    var attitudeInput by remember { mutableStateOf("Alerta") }
    var consciousnessInput by remember { mutableStateOf("Consciente") }

    // 12 Sistemas Orgánicos
    var mucosas by remember { mutableStateOf("Rosadas y húmedas") }
    var ojos by remember { mutableStateOf("Normales") }
    var oidos by remember { mutableStateOf("Limpios") }
    var nodulos by remember { mutableStateOf("No reactivos") }
    var piel by remember { mutableStateOf("Pelaje sano") }
    var locomocion by remember { mutableStateOf("Sin claudicación") }
    var musculo by remember { mutableStateOf("Sin dolor") }
    var nervioso by remember { mutableStateOf("Reflejos normales") }
    var cardio by remember { mutableStateOf("Sin soplos") }
    var resp by remember { mutableStateOf("Campos limpios") }
    var digestivo by remember { mutableStateOf("Abdomen blando") }
    var genito by remember { mutableStateOf("Vejiga normal") }

    // Diagnósticos
    var problemListText by remember { mutableStateOf("") }
    var diffDiag by remember { mutableStateOf("") }
    var defDiag by remember { mutableStateOf("") }
    var treatPlan by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var nextDate by remember { mutableStateOf("") }

    // Medicamentos y Receta
    var drugName by remember { mutableStateOf("") }
    var drugDose by remember { mutableStateOf("") }
    var drugRoute by remember { mutableStateOf("Oral") }
    var drugFreq by remember { mutableStateOf("Cada 12h") }
    var drugDuration by remember { mutableStateOf("7 días") }
    var drugEndDate by remember { mutableStateOf("") }
    var isConsultationAdmin by remember { mutableStateOf(false) }

    // Estudios
    var studyName by remember { mutableStateOf("") }
    var studyType by remember { mutableStateOf("LABORATORY") }

    val coroutineScope = rememberCoroutineScope()
    val repository = remember { DefaultMedicalRecordRepository(SupabaseMedicalRecordDataSource(supabase)) }

    LaunchedEffect(Unit) {
        isLoadingPatients = true
        try {
            val user = supabase.auth.currentSessionOrNull()?.user
                ?: error("No hay una sesión activa en Supabase.")
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", user.id) } }
                .decodeSingle<UserProfile>()
            if (profile.role != ProfileRole.VETERINARY_BUSINESS && profile.role != ProfileRole.SUPERADMIN) {
                error("Acceso restringido a médicos veterinarios.")
            }
            tenantId = resolveTenantId(supabase, user.id, profile)
            vetUserId = user.id
            vetUserName = profile.displayName.ifBlank { "Dr. Veterinario" }

            val pets = supabase.postgrest["pets"]
                .select()
                .decodeList<PatientRow>()
            patientsList = pets
            if (pets.isNotEmpty()) {
                selectedPatient = pets.first()
            }
        } catch (e: Exception) {
            statusMessage = e.message ?: "Error al cargar módulo de consultas."
        } finally {
            isLoadingPatients = false
        }
    }

    LaunchedEffect(selectedPatient) {
        val ownerId = selectedPatient?.ownerId
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

    if (showLinkOrRegisterDialog) {
        LinkOrRegisterPatientDialog(
            onDismiss = { showLinkOrRegisterDialog = false },
            onPatientSelected = { newPatient ->
                if (patientsList.none { it.id == newPatient.id }) {
                    patientsList = listOf(newPatient) + patientsList
                }
                selectedPatient = newPatient
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
            }
        )
    }

    VetAppLayout(
        currentScreen = "Consultas",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = "Atención de Consultas Clínicas",
        userEmail = userEmail
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PANEL IZQUIERDO: Buscar y Seleccionar Paciente por medical_id (0.32f)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pacientes Registrados",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

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

                    if (isLoadingPatients) {
                        Text("Cargando pacientes…", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                    } else if (filtered.isEmpty()) {
                        Text("No se encontraron pacientes.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 12.dp))
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

            // PANEL DERECHO: Formulario Completo de Consulta (0.68f)
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
                    val p = selectedPatient
                    if (p != null) {
                        val medId = p.medicalId ?: "PM-${p.id.take(4).uppercase()}"
                        val pName = p.name ?: "Mascota"
                        val pSpecies = p.species ?: "Especie"
                        val pBreed = p.breed ?: "Mestizo"
                        val ownerNameStr = listOfNotNull(selectedOwner?.firstName, selectedOwner?.lastName).joinToString(" ").ifBlank {
                            selectedOwner?.displayName ?: "Propietario Registrado"
                        }

                        // HEADER PACIENTE SELECCIONADO
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldLightBg)
                                        .border(2.dp, ClinicalEmerald, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(pName.take(2).uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SelectedText)
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(pName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(ClinicalEmerald)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text("ID Médico: $medId", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                    Text("$pSpecies • $pBreed • Propietario: $ownerNameStr", fontSize = 12.sp, color = TextSecondary)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { showEditPetDialog = true },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Editar Paciente", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                }

                                Button(
                                    onClick = {
                                        if (reason.isBlank()) {
                                            statusMessage = "El motivo de consulta es obligatorio."
                                            return@Button
                                        }

                                        coroutineScope.launch {
                                            isSavingConsultation = true
                                            statusMessage = null
                                            successMessage = null
                                            try {
                                                val todayIso = convertMillisToIsoDateString(System.currentTimeMillis())
                                                val defaultExpiry = convertMillisToIsoDateString(System.currentTimeMillis() + 7L * 86400000L)

                                                val vitals = VitalSigns(
                                                    temperatureCelsius = tempInput.toDoubleOrNull(),
                                                    heartRateBpm = fcInput.toIntOrNull(),
                                                    respiratoryRateRpm = frInput.toIntOrNull(),
                                                    pulseQuality = pulseInput,
                                                    weightGrams = ((weightInput.toDoubleOrNull() ?: 4.2) * 1000).toInt(),
                                                    tllcSeconds = tllcInput,
                                                    hydrationStatus = hydrationInput,
                                                    bodyConditionScore = bcsInput,
                                                    attitude = attitudeInput,
                                                    consciousnessState = consciousnessInput
                                                )

                                                val systems = SystemsExploration(
                                                    mucosas = mucosas, ojos = ojos, oidos = oidos, nodulosLinfaticos = nodulos,
                                                    pielAnexos = piel, locomocion = locomocion, musculoEsqueletico = musculo,
                                                    sistemaNervioso = nervioso, sistemaCardiovascular = cardio,
                                                    sistemaRespiratorio = resp, sistemaDigestivo = digestivo, sistemaGenitourinario = genito
                                                )

                                                val consultation = ClinicalConsultation(
                                                    petId = p.id,
                                                    tenantId = tenantId,
                                                    veterinarianId = vetUserId ?: "",
                                                    veterinarianName = vetUserName,
                                                    reason = reason.trim(),
                                                    anamnesis = anamnesis.trim(),
                                                    vitalSigns = vitals,
                                                    systemsExploration = systems,
                                                    problemList = problemListText.split("\n", ",").map { it.trim() }.filter { it.isNotBlank() },
                                                    differentialDiagnoses = diffDiag.trim(),
                                                    definitiveDiagnosis = defDiag.trim(),
                                                    treatmentPlan = treatPlan.trim(),
                                                    observations = observations.trim(),
                                                    nextAppointmentDate = nextDate.ifBlank { null },
                                                    occurredAt = todayIso
                                                )

                                                val prescriptions = if (drugName.isNotBlank()) {
                                                    listOf(
                                                        PrescriptionRecord(
                                                            petId = p.id,
                                                            drugName = drugName.trim(),
                                                            dosage = drugDose.trim(),
                                                            route = drugRoute.trim(),
                                                            frequency = drugFreq.trim(),
                                                            duration = drugDuration.trim(),
                                                            startDate = todayIso,
                                                            endDate = drugEndDate.ifBlank { defaultExpiry },
                                                            isAdministeredInConsultation = isConsultationAdmin,
                                                            prescribingVetName = vetUserName,
                                                            prescribingVetId = vetUserId
                                                        )
                                                    )
                                                } else emptyList()

                                                val studies = if (studyName.isNotBlank()) {
                                                    listOf(
                                                        ClinicalStudyRecord(
                                                            petId = p.id,
                                                            studyType = studyType,
                                                            name = studyName.trim(),
                                                            status = "COMPLETED"
                                                        )
                                                    )
                                                } else emptyList()

                                                repository.saveConsultation(consultation, prescriptions, studies)
                                                    .onSuccess {
                                                        successMessage = "¡Consulta registrada exitosamente para $pName (ID: $medId)!"
                                                        reason = ""
                                                        anamnesis = ""
                                                        defDiag = ""
                                                        treatPlan = ""
                                                        drugName = ""
                                                        drugDose = ""
                                                        drugEndDate = ""
                                                        studyName = ""
                                                    }
                                                    .onFailure { err ->
                                                        statusMessage = err.message ?: "No se pudo guardar la consulta."
                                                    }
                                            } catch (e: Exception) {
                                                statusMessage = e.message ?: "Error inesperado al guardar."
                                            } finally {
                                                isSavingConsultation = false
                                            }
                                        }
                                    },
                                    enabled = !isSavingConsultation,
                                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                                ) {
                                    Icon(Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isSavingConsultation) "Guardando…" else "Guardar Consulta", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (statusMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(statusMessage!!, color = Color(0xFFB91C1C), fontSize = 12.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp)).padding(8.dp))
                        }

                        if (successMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(Color(0xFFECFDF5), RoundedCornerShape(6.dp)).padding(8.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ClinicalEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(successMessage!!, color = ClinicalEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // SECTOR SELECTOR PASOS FORMULARIO (4 PASOS)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("1. Anamnesis & Motivo", "2. Constantes & Sistemas", "3. Diagnósticos", "4. Recetas & Estudios").forEachIndexed { idx, label ->
                                val isSel = formSection == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) EmeraldGreen else SelectedBg)
                                        .border(1.dp, if (isSel) EmeraldGreen else BorderLight, RoundedCornerShape(6.dp))
                                        .clickable { formSection = idx }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = if (isSel) Color.White else TextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // CONTENIDO DEL PASO
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            when (formSection) {
                                0 -> {
                                    Text("MOTIVO DE CONSULTA Y ANAMNESIS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Motivo de consulta (*Requerido)") }, placeholder = { Text("Ej. Vomitos, vacuna, control") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                                    OutlinedTextField(value = anamnesis, onValueChange = { anamnesis = it }, label = { Text("Anamnesis / Historia Reportada por Propietario") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(8.dp))
                                }

                                1 -> {
                                    Text("CONSTANTES FISIOLÓGICAS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = tempInput, onValueChange = { tempInput = it }, label = { Text("Temp (°C)") }) }
                                        Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = fcInput, onValueChange = { fcInput = it }, label = { Text("FC (bpm)") }) }
                                        Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = frInput, onValueChange = { frInput = it }, label = { Text("FR (rpm)") }) }
                                        Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = weightInput, onValueChange = { weightInput = it }, label = { Text("Peso (kg)") }) }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("EXPLORACIÓN DE 12 SISTEMAS ORGÁNICOS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)

                                    val systems = listOf(
                                        "Mucosas" to mucosas, "Ojos" to ojos, "Oídos" to oidos, "Nódulos" to nodulos,
                                        "Piel" to piel, "Locomoción" to locomocion, "Músculo-Esquelético" to musculo,
                                        "Nervioso" to nervioso, "Cardiovascular" to cardio, "Respiratorio" to resp,
                                        "Digestivo" to digestivo, "Genitourinario" to genito
                                    )

                                    systems.chunked(2).forEach { pair ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            pair.forEach { (lbl, valStr) ->
                                                Column(modifier = Modifier.weight(1f)) {
                                                    OutlinedTextField(
                                                        value = valStr,
                                                        onValueChange = { newV ->
                                                            when (lbl) {
                                                                "Mucosas" -> mucosas = newV
                                                                "Ojos" -> ojos = newV
                                                                "Oídos" -> oidos = newV
                                                                "Nódulos" -> nodulos = newV
                                                                "Piel" -> piel = newV
                                                                "Locomoción" -> locomocion = newV
                                                                "Músculo-Esquelético" -> musculo = newV
                                                                "Nervioso" -> nervioso = newV
                                                                "Cardiovascular" -> cardio = newV
                                                                "Respiratorio" -> resp = newV
                                                                "Digestivo" -> digestivo = newV
                                                                "Genitourinario" -> genito = newV
                                                            }
                                                        },
                                                        label = { Text(lbl) },
                                                        singleLine = true
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                2 -> {
                                    Text("DIAGNÓSTICOS Y PLAN TRATAMIENTO", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                    OutlinedTextField(value = problemListText, onValueChange = { problemListText = it }, label = { Text("Lista de Problemas (separados por coma)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                                    OutlinedTextField(value = diffDiag, onValueChange = { diffDiag = it }, label = { Text("Diagnósticos Diferenciales") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                                    OutlinedTextField(value = defDiag, onValueChange = { defDiag = it }, label = { Text("Diagnóstico Definitivo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                                    OutlinedTextField(value = treatPlan, onValueChange = { treatPlan = it }, label = { Text("Plan de Tratamiento") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                                    OutlinedTextField(value = observations, onValueChange = { observations = it }, label = { Text("Observaciones Generales") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                                    OutlinedTextField(value = nextDate, onValueChange = { nextDate = it }, label = { Text("Próxima Cita / Seguimiento (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                                }

                                3 -> {
                                    Text("EMISIÓN DE RECETA MÉDICA", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                    OutlinedTextField(value = drugName, onValueChange = { drugName = it }, label = { Text("Nombre del Medicamento") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = drugDose, onValueChange = { drugDose = it }, label = { Text("Dosis (ej. 50mg)") }) }
                                        Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = drugRoute, onValueChange = { drugRoute = it }, label = { Text("Vía (Oral, IV)") }) }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = drugFreq, onValueChange = { drugFreq = it }, label = { Text("Frecuencia (c/12h)") }) }
                                        Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = drugDuration, onValueChange = { drugDuration = it }, label = { Text("Duración (7 días)") }) }
                                    }

                                    OutlinedTextField(
                                        value = drugEndDate,
                                        onValueChange = { drugEndDate = it },
                                        label = { Text("Fecha Caducidad / Vencimiento Receta (*Obligatoria)") },
                                        placeholder = { Text("AAAA-MM-DD (ej. 2026-10-15)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isConsultationAdmin, onCheckedChange = { isConsultationAdmin = it })
                                        Text("Administrado en consulta clínica", fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("SOLICITUD DE ESTUDIO CLÍNICO", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                    OutlinedTextField(value = studyName, onValueChange = { studyName = it }, label = { Text("Nombre del Estudio (ej. Biometría, Ecografía)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No hay paciente seleccionado.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Busca o vincula a un paciente usando su ID Médico (`medical_id`) a la izquierda para iniciar la consulta.", color = TextSecondary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showLinkOrRegisterDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("➕ Vincular / Registrar Paciente", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
