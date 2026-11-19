package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pawsmedic.desktop.admin.translation.translateBatchLocally
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import com.pawsmedic.desktop.veterinary.model.OwnerProfileRow
import com.pawsmedic.desktop.veterinary.model.PatientRow
import com.pawsmedic.desktop.veterinary.model.resolveTenantId
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.dialog.EditPetDataDialog
import com.pawsmedic.desktop.veterinary.ui.dialog.LinkOrRegisterPatientDialog
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.features.medical_records.data.datasource.SupabaseMedicalRecordDataSource
import com.pawsmedic.features.medical_records.data.repository.DefaultMedicalRecordRepository
import com.pawsmedic.features.medical_records.domain.model.PetMedicalRecord
import com.pawsmedic.features.medical_records.presentation.dialog.EditBackgroundDialog
import com.pawsmedic.shared.core.common.calculateAgeFromIsoDate
import com.pawsmedic.shared.core.model.ProfileRole
import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
private data class NewClinicalConsultation(
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("pet_id") val petId: String,
    @SerialName("veterinarian_id") val veterinarianId: String,
    val reason: String,
    val observations: String,
    val diagnosis: String,
    val plan: String,
    @SerialName("idempotency_key") val idempotencyKey: String
)

@Composable
fun ExpedientesScreen(
    userEmail: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, translated: String) = if (english) translated else spanish

    var searchMedicalIdQuery by remember { mutableStateOf("") }
    var patientsList by remember { mutableStateOf<List<PatientRow>>(emptyList()) }
    var selectedPatient by remember { mutableStateOf<PatientRow?>(null) }
    var selectedOwner by remember { mutableStateOf<OwnerProfileRow?>(null) }
    var fullRecord by remember { mutableStateOf<PetMedicalRecord?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Antecedentes, 1: Consultas SOAP, 2: TODAS las Recetas, 3: Examen Físico, 4: Estudios, 5: Bitácora

    var tenantId by remember { mutableStateOf<String?>(null) }
    var veterinarianId by remember { mutableStateOf<String?>(null) }
    var vetUserId by remember { mutableStateOf<String?>(null) }
    var vetUserName by remember { mutableStateOf("Dr. Veterinario") }

    var isLoading by remember { mutableStateOf(true) }
    var isLoadingRecord by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var showEditBackgroundDialog by remember { mutableStateOf(false) }
    var showLinkOrRegisterDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var showConsultationDialog by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var petIdInput by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("") }

    val translations = remember(userEmail) { mutableStateMapOf<String, String>() }
    var showOriginal by remember { mutableStateOf(false) }
    var translationError by remember { mutableStateOf(false) }
    var translating by remember { mutableStateOf(false) }
    var translationAttempt by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val repository = remember { DefaultMedicalRecordRepository(SupabaseMedicalRecordDataSource(supabase)) }

    val textsToTranslate = remember(fullRecord) {
        val rec = fullRecord
        if (rec == null) emptyList()
        else {
            val bg = listOf(rec.background.diet, rec.background.allergies, rec.background.pastDiseases, rec.background.pastSurgeries, rec.background.familyHistory, rec.background.habitat)
            val consultationsTexts = rec.consultations.flatMap { listOf(it.reason, it.anamnesis, it.definitiveDiagnosis, it.treatmentPlan) }
            val presTexts = rec.prescriptions.flatMap { listOf(it.drugName, it.notes) }
            val studyTexts = rec.studies.flatMap { listOf(it.name, it.resultsNotes) }
            (bg + consultationsTexts + presTexts + studyTexts).filter { it.isNotBlank() }
        }
    }

    LaunchedEffect(english, textsToTranslate, translationAttempt) {
        if (!english) {
            translating = false
            translationError = false
            return@LaunchedEffect
        }
        val missing = textsToTranslate.filter { it.isNotBlank() && !translations.containsKey(it) }.distinct()
        val pending = missing.filter { it.length <= 4000 }
        translationError = pending.size != missing.size
        if (pending.isNotEmpty()) {
            translating = true
            try {
                for (batch in pending.chunked(50)) {
                    val results = translateBatchLocally(batch)
                    batch.zip(results).forEach { (original, translated) -> translations[original] = translated }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                translationError = true
            } finally {
                translating = false
            }
        }
    }

    fun displayText(value: String): String =
        if (english && !showOriginal) translations[value] ?: value else value

    fun loadFullRecord(petId: String) {
        coroutineScope.launch {
            isLoadingRecord = true
            repository.getPetMedicalRecord(petId)
                .onSuccess { rec ->
                    fullRecord = rec
                    isLoadingRecord = false
                }
                .onFailure { err ->
                    errorMessage = err.message ?: tr("No se pudo cargar el expediente.", "Could not load medical record.")
                    isLoadingRecord = false
                }
        }
    }

    suspend fun loadRecords() {
        isLoading = true
        errorMessage = null
        try {
            val user = supabase.auth.currentSessionOrNull()?.user
                ?: error(tr("No hay una sesión activa en Supabase.", "No active Supabase session."))
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", user.id) } }
                .decodeSingle<UserProfile>()
            if (profile.role != ProfileRole.VETERINARY_BUSINESS && profile.role != ProfileRole.SUPERADMIN) {
                error(tr("Los expedientes solo están disponibles para cuentas veterinarias.", "Medical records are only available to veterinary accounts."))
            }
            vetUserId = user.id
            vetUserName = profile.displayName.ifBlank { "Dr. Veterinario" }
            veterinarianId = user.id

            val clinicId = resolveTenantId(supabase, user.id, profile)
            tenantId = clinicId

            val pets = supabase.postgrest["pets"]
                .select()
                .decodeList<PatientRow>()
            patientsList = pets
            if (pets.isNotEmpty()) {
                selectedPatient = pets.first()
                loadFullRecord(pets.first().id)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            System.err.println("No se pudieron cargar los expedientes: ${e.javaClass.simpleName}")
            errorMessage = tr(
                "No se pudieron cargar los expedientes. Comprueba tu sesión, los permisos y la conexión.",
                "Could not load records. Check your session, permissions, and connection."
            )
        } finally {
            isLoading = false
        }
    }

    suspend fun createConsultation() {
        val clinicId = tenantId ?: run { errorMessage = tr("No hay una clínica autorizada.", "No clinic is authorized."); return }
        val userId = veterinarianId ?: run { errorMessage = tr("No se encontró el veterinario de la sesión.", "No veterinarian was found for this session."); return }
        val petId = petIdInput.trim().ifBlank { selectedPatient?.id ?: "" }
        if (runCatching { UUID.fromString(petId) }.isFailure) { errorMessage = tr("Ingresa un ID de mascota UUID válido.", "Enter a valid pet UUID."); return }
        if (reason.isBlank() || observations.isBlank()) { errorMessage = tr("El motivo y las observaciones son obligatorios.", "Reason and observations are required."); return }
        isSaving = true
        errorMessage = null
        try {
            supabase.postgrest["clinical_consultations"].insert(
                NewClinicalConsultation(
                    tenantId = clinicId,
                    petId = petId,
                    veterinarianId = userId,
                    reason = reason.trim(),
                    observations = observations.trim(),
                    diagnosis = diagnosis.trim(),
                    plan = plan.trim(),
                    idempotencyKey = UUID.randomUUID().toString()
                )
            )
            showConsultationDialog = false
            petIdInput = ""
            reason = ""
            observations = ""
            diagnosis = ""
            plan = ""
            selectedPatient?.let { loadFullRecord(it.id) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            System.err.println("No se pudo guardar la consulta: ${e.javaClass.simpleName}")
            errorMessage = tr("No se pudo guardar la consulta. Comprueba los permisos y la conexión.", "Could not save consultation. Check permissions and connection.")
        } finally {
            isSaving = false
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
                    repository.updateBackground(updatedBg.copy(updatedBy = vetUserId ?: vetUserName))
                        .onSuccess {
                            showEditBackgroundDialog = false
                            selectedPatient?.let { loadFullRecord(it.id) }
                        }
                        .onFailure { err ->
                            errorMessage = err.message ?: tr("No se pudieron guardar los antecedentes en Supabase.", "Could not save background in Supabase.")
                            showEditBackgroundDialog = false
                        }
                }
            }
        )
    }

    if (showConsultationDialog) {
        Dialog(onDismissRequest = { if (!isSaving) showConsultationDialog = false }) {
            Card(
                modifier = Modifier.width(540.dp).wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            tr("Registrar consulta clínica", "Create clinical consultation"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = { if (!isSaving) showConsultationDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = tr("Cerrar", "Close"))
                        }
                    }
                    Text(
                        tr("Se guarda en clinical_consultations como borrador.", "Saves a draft in clinical_consultations."),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = if (petIdInput.isBlank() && selectedPatient != null) selectedPatient!!.id else petIdInput,
                        onValueChange = { petIdInput = it },
                        label = { Text(tr("ID de mascota (UUID)", "Pet ID (UUID)")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text(tr("Motivo de consulta", "Reason for consultation")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = observations,
                        onValueChange = { observations = it },
                        label = { Text(tr("Observaciones clínicas", "Clinical observations")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = diagnosis,
                        onValueChange = { diagnosis = it },
                        label = { Text(tr("Diagnóstico", "Diagnosis")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = plan,
                        onValueChange = { plan = it },
                        label = { Text(tr("Plan de tratamiento", "Treatment plan")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showConsultationDialog = false },
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(tr("Cancelar", "Cancel"))
                        }
                        Button(
                            onClick = { coroutineScope.launch { createConsultation() } },
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                        ) {
                            Text(
                                if (isSaving) tr("Guardando…", "Saving…") else tr("Guardar borrador", "Save draft"),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { loadRecords() }

    VetAppLayout(
        currentScreen = "Expedientes",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = tr("Expedientes Clínicos", "Clinical records"),
        userEmail = userEmail
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (english) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Machine translation may be inaccurate. Verify the original before clinical decisions.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showOriginal = !showOriginal }) {
                        Text(if (showOriginal) "Show translation" else "Show original")
                    }
                    if (translating) {
                        Text("Translating…", color = TextSecondary, fontSize = 12.sp)
                    }
                    if (translationError) {
                        Text("Translation error", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        TextButton(onClick = { translationAttempt++ }) { Text("Retry") }
                    }
                }
            }

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
                        .background(CardWhite)
                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = tr("Pacientes Registrados", "Registered Patients"),
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
                            Text(tr("➕ Vincular por ID Médico (`medical_id`)", "➕ Link by Medical ID (`medical_id`)"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // BÚSQUEDA POR MEDICAL_ID O NOMBRE
                        OutlinedTextField(
                            value = searchMedicalIdQuery,
                            onValueChange = { searchMedicalIdQuery = it },
                            placeholder = { Text(tr("Buscar por ID Médico (PM-XXXX) o Nombre", "Search by Medical ID (PM-XXXX) or Name"), fontSize = 12.sp) },
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
                            Text(tr("Cargando pacientes…", "Loading patients…"), fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                        } else if (filtered.isEmpty()) {
                            Text(tr("No se encontraron expedientes.", "No records found."), fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 12.dp))
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filtered) { p ->
                                    val isSelected = selectedPatient?.id == p.id
                                    val medIdStr = p.medicalId ?: "PM-${p.id.take(4).uppercase()}"
                                    val nameStr = p.name ?: tr("Mascota", "Pet")
                                    val speciesStr = p.species ?: tr("Especie", "Species")
                                    val breedStr = p.breed ?: tr("Mestizo", "Mixed Breed")

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) SelectedBg else CardWhite)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) EmeraldGreen else BorderLight,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedPatient = p
                                                loadFullRecord(p.id)
                                            }
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
                        .background(CardWhite)
                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        val p = selectedPatient
                        val rec = fullRecord

                        if (p != null) {
                            val medId = p.medicalId ?: "PM-${p.id.take(4).uppercase()}"
                            val pName = p.name ?: tr("Mascota", "Pet")
                            val pSpecies = p.species ?: tr("Especie", "Species")
                            val pBreed = p.breed ?: tr("Mestizo", "Mixed Breed")
                            val computedAge = calculateAgeFromIsoDate(p.dateOfBirth) ?: tr("Edad no especificada", "Age not specified")
                            val ownerNameStr = listOfNotNull(selectedOwner?.firstName, selectedOwner?.lastName).joinToString(" ").ifBlank {
                                selectedOwner?.displayName ?: tr("Propietario Registrado", "Registered Owner")
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
                                                Text("${tr("ID Médico:", "Medical ID:")} $medId", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                        Text("$pSpecies • $pBreed • ${tr("Edad:", "Age:")} $computedAge", fontSize = 12.sp, color = TextSecondary)
                                        Text("${tr("Propietario:", "Owner:")} $ownerNameStr • ${tr("Tel:", "Phone:")} ${selectedOwner?.phone ?: "N/A"}", fontSize = 11.sp, color = TextMuted)
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
                                        Text(tr("Editar Paciente", "Edit Patient"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                    }

                                    OutlinedButton(
                                        onClick = { showEditBackgroundDialog = true },
                                        modifier = Modifier.height(34.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tr("Editar Antecedentes", "Edit Background"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                    }

                                    Button(
                                        onClick = { showConsultationDialog = true },
                                        modifier = Modifier.height(34.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tr("Nueva Consulta", "New Consultation"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(6.dp))
                                        .padding(6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // PESTAÑAS DEL EXPEDIENTE
                            val tabs = listOf(
                                tr("Antecedentes Médicos", "Medical Background"),
                                tr("Consultas (SOAP)", "Consultations (SOAP)"),
                                tr("TODAS las Recetas", "All Prescriptions"),
                                tr("Examen Físico & 12 Sistemas", "Physical Exam & 12 Systems"),
                                tr("Estudios & Adjuntos", "Studies & Attachments"),
                                tr("Bitácora Auditoría", "Audit Log")
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
                                Text(tr("Cargando expediente de $pName…", "Loading medical record for $pName…"), fontSize = 13.sp, color = TextSecondary)
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
                                            Text(tr("ANTECEDENTES CLÍNICOS", "CLINICAL BACKGROUND"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                            Text("${tr("Alimentación:", "Diet:")} ${displayText(bg.diet.ifBlank { tr("Croquetas comerciales", "Commercial kibble") })}", fontSize = 13.sp, color = TextPrimary)
                                            Text("${tr("Alergias Conocidas:", "Known Allergies:")} ${displayText(bg.allergies.ifBlank { p.allergies ?: tr("Ninguna reportada", "None reported") })}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                            Text("${tr("Última Desparasitación:", "Last Deworming:")} ${bg.lastDewormingDate ?: tr("No registrada", "Not recorded")} (${bg.dewormingProduct})", fontSize = 13.sp, color = TextPrimary)
                                            Text("${tr("Enfermedades Anteriores:", "Past Diseases:")} ${displayText(bg.pastDiseases.ifBlank { tr("Ninguna", "None") })}", fontSize = 13.sp, color = TextPrimary)
                                            Text("${tr("Cirugías Anteriores:", "Past Surgeries:")} ${displayText(bg.pastSurgeries.ifBlank { tr("Ninguna", "None") })}", fontSize = 13.sp, color = TextPrimary)
                                            Text("${tr("Antecedentes Familiares:", "Family History:")} ${displayText(bg.familyHistory.ifBlank { tr("Sin datos", "No data") })}", fontSize = 13.sp, color = TextPrimary)
                                            Text("${tr("Hábitat:", "Habitat:")} ${displayText(bg.habitat.ifBlank { tr("Interior sin acceso a calle", "Indoor without street access") })}", fontSize = 13.sp, color = TextPrimary)
                                        }

                                        1 -> { // CONSULTAS SOAP
                                            Text(tr("CONSULTAS CLÍNICAS (SOAP)", "CLINICAL CONSULTATIONS (SOAP)"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                            if (rec.consultations.isEmpty()) {
                                                Text(tr("Sin consultas clínicas registradas previamente.", "No clinical consultations recorded previously."), fontSize = 13.sp, color = TextMuted)
                                            } else {
                                                rec.consultations.forEach { c ->
                                                    Column(modifier = Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp)) {
                                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                            Text("${tr("Fecha:", "Date:")} ${c.occurredAt} • Vet: ${c.veterinarianName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                            Text("${tr("Estado:", "Status:")} ${c.status}", fontSize = 11.sp, color = TextMuted)
                                                        }
                                                        HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))
                                                        Text("${tr("Motivo:", "Reason:")} ${displayText(c.reason)}", fontSize = 13.sp, color = TextPrimary)
                                                        if (c.anamnesis.isNotBlank()) Text("${tr("Anamnesis:", "Anamnesis:")} ${displayText(c.anamnesis)}", fontSize = 12.sp, color = TextSecondary)
                                                        if (c.definitiveDiagnosis.isNotBlank()) Text("${tr("Diagnóstico:", "Diagnosis:")} ${displayText(c.definitiveDiagnosis)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                                        if (c.treatmentPlan.isNotBlank()) Text("${tr("Tratamiento:", "Treatment:")} ${displayText(c.treatmentPlan)}", fontSize = 12.sp, color = TextPrimary)
                                                    }
                                                }
                                            }
                                        }

                                        2 -> { // TODAS LAS RECETAS
                                            Text(tr("RECETAS MÉDICAS Y FÁRMACOS (HISTORIAL COMPLETO)", "PRESCRIPTIONS AND MEDICATIONS (FULL HISTORY)"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                            if (rec.prescriptions.isEmpty()) {
                                                Text(tr("Sin recetas registradas para este paciente.", "No prescriptions registered for this patient."), fontSize = 13.sp, color = TextMuted)
                                            } else {
                                                rec.prescriptions.forEach { pres ->
                                                    Column(modifier = Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp)) {
                                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                            Text(displayText(pres.drugName), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(if (pres.isActive) EmeraldLightBg else OffWhiteBg)
                                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (pres.isActive) tr("🟢 ACTIVA (Vence: ${pres.endDate ?: "N/A"})", "🟢 ACTIVE (Expires: ${pres.endDate ?: "N/A"})") else tr("⚪ VENCIDA (Venció: ${pres.endDate ?: "N/A"})", "⚪ EXPIRED (Expired: ${pres.endDate ?: "N/A"})"),
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (pres.isActive) EmeraldGreen else TextMuted
                                                                )
                                                            }
                                                        }
                                                        Text("${tr("Dosis:", "Dosage:")} ${pres.dosage} ${pres.unit} • ${tr("Vía:", "Route:")} ${pres.route} • ${tr("Frecuencia:", "Frequency:")} ${pres.frequency} • ${tr("Duración:", "Duration:")} ${pres.duration}", fontSize = 12.sp, color = TextSecondary)
                                                        Text("${tr("Emitida por:", "Issued by:")} ${pres.prescribingVetName.ifBlank { "Dr. Veterinario" }}", fontSize = 11.sp, color = TextMuted)
                                                        if (pres.notes.isNotBlank()) Text("${tr("Instrucciones:", "Instructions:")} ${displayText(pres.notes)}", fontSize = 12.sp, color = TextPrimary)
                                                    }
                                                }
                                            }
                                        }

                                        3 -> { // EXAMEN FISICO
                                            val latest = rec.consultations.firstOrNull()
                                            if (latest == null) {
                                                Text(tr("Sin datos de examen físico recientes.", "No recent physical exam data."), fontSize = 13.sp, color = TextMuted)
                                            } else {
                                                val v = latest.vitalSigns
                                                val s = latest.systemsExploration
                                                Text(tr("CONSTANTES FISIOLÓGICAS RECIENTES", "RECENT VITAL SIGNS"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                                Text("Temp: ${v.temperatureCelsius ?: 38.5} °C • FC: ${v.heartRateBpm ?: 120} bpm • FR: ${v.respiratoryRateRpm ?: 24} rpm • Peso: ${(v.weightGrams ?: 4200) / 1000.0} kg", fontSize = 13.sp, color = TextPrimary)
                                                Text("TLLC: ${v.tllcSeconds} • Hidratación: ${v.hydrationStatus} • Condición: ${v.bodyConditionScore}", fontSize = 12.sp, color = TextSecondary)

                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(tr("EXPLORACIÓN DE 12 SISTEMAS ORGÁNICOS", "12 SYSTEMS EXPLORATION"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                                Text("Mucosas: ${s.mucosas}", fontSize = 12.sp, color = TextPrimary)
                                                Text("Ojos: ${s.ojos} • Oídos: ${s.oidos}", fontSize = 12.sp, color = TextPrimary)
                                                Text("Piel y Anexos: ${s.pielAnexos}", fontSize = 12.sp, color = TextPrimary)
                                                Text("Sistema Respiratorio: ${s.sistemaRespiratorio}", fontSize = 12.sp, color = TextPrimary)
                                                Text("Sistema Digestivo: ${s.sistemaDigestivo}", fontSize = 12.sp, color = TextPrimary)
                                            }
                                        }

                                        4 -> { // ESTUDIOS
                                            Text(tr("ESTUDIOS CLÍNICOS E IMÁGENES", "CLINICAL STUDIES AND IMAGES"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                            if (rec.studies.isEmpty()) {
                                                Text(tr("Sin estudios o imágenes adjuntas.", "No studies or images attached."), fontSize = 13.sp, color = TextMuted)
                                            } else {
                                                rec.studies.forEach { st ->
                                                    Text("${tr("Estudio:", "Study:")} ${displayText(st.name)} (${st.studyType}) - ${tr("Fecha:", "Date:")} ${st.requestedAt}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                    if (st.resultsNotes.isNotBlank()) Text("${tr("Resultados:", "Results:")} ${displayText(st.resultsNotes)}", fontSize = 12.sp, color = TextSecondary)
                                                }
                                            }
                                        }

                                        5 -> { // AUDITORIA
                                            Text(tr("BITÁCORA DE AUDITORÍA Y TRAZABILIDAD", "AUDIT LOG AND TRACEABILITY"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                                            if (rec.auditLogs.isEmpty()) {
                                                Text(tr("Sin registros de auditoría para este expediente.", "No audit logs for this record."), fontSize = 13.sp, color = TextMuted)
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
                                Text(tr("Selecciona un paciente a la izquierda para ver su expediente.", "Select a patient on the left to view their medical record."), color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
