package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Healing
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
import com.pawsmedic.desktop.veterinary.model.OwnerProfileRow
import com.pawsmedic.desktop.veterinary.model.PatientRow
import com.pawsmedic.desktop.veterinary.model.resolveTenantId
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.dialog.EditPetDataDialog
import com.pawsmedic.desktop.veterinary.ui.dialog.LinkOrRegisterPatientDialog
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import com.pawsmedic.features.medical_records.data.datasource.SupabaseMedicalRecordDataSource
import com.pawsmedic.features.medical_records.data.repository.DefaultMedicalRecordRepository
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
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, englishText: String) = if (english) englishText else spanish
    var searchMedicalIdQuery by remember { mutableStateOf("") }
    var patientsList by remember { mutableStateOf<List<PatientRow>>(emptyList()) }
    var selectedPatient by remember { mutableStateOf<PatientRow?>(null) }
    var selectedOwner by remember { mutableStateOf<OwnerProfileRow?>(null) }
    var tenantId by remember { mutableStateOf<String?>(null) }
    var vetUserId by remember { mutableStateOf<String?>(null) }
    var vetUserName by remember { mutableStateOf("Dr. Veterinario") }
    var isLoadingPatients by remember { mutableStateOf(true) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showLinkOrRegisterDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

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

    if (showCategoryDialog && selectedPatient != null) {
        Dialog(onDismissRequest = { showCategoryDialog = false }) {
            Card(
                modifier = Modifier.width(480.dp).wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(tr("Selecciona el tipo de atención", "Select the type of care"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        tr(
                            "Se abrirá la plantilla correspondiente para ${selectedPatient?.name ?: "la mascota"}.",
                            "The corresponding template will open for ${selectedPatient?.name ?: "the pet"}."
                        ),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val categories = listOf(
                        Triple("Consulta Médica General (SOAP)", "General medical consultation (SOAP)", "CONSULTA_GENERAL"),
                        Triple("Cirugía y Procedimientos", "Surgery and procedures", "CIRUGIA"),
                        Triple("Medicina Preventiva / Vacunación", "Preventive medicine / vaccination", "VACUNACION"),
                        Triple("Estética y Grooming", "Grooming", "ESTETICA"),
                        Triple("Diagnóstico e Imagenología", "Diagnostics and imaging", "DIAGNOSTICO"),
                        Triple("Hospitalización y Monitoreo", "Hospitalization and monitoring", "HOSPITALIZACION"),
                        Triple("Gestión Administrativa", "Administrative management", "ADMINISTRATIVA")
                    )

                    categories.forEach { (label, englishLabel, code) ->
                        Button(
                            onClick = {
                                showCategoryDialog = false
                                VeterinaryCareContext.appointmentId = null
                                VeterinaryCareContext.patient = selectedPatient
                                VeterinaryCareContext.serviceName = label
                                VeterinaryCareContext.categoryType = code
                                VeterinaryCareContext.tenantId = tenantId
                                VeterinaryCareContext.vetUserId = vetUserId
                                VeterinaryCareContext.vetUserName = vetUserName
                                VeterinaryCareContext.prefilledReason = label
                                onNavigate("VeterinaryCare")
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldLightBg)
                        ) {
                            Text(if (english) englishLabel else label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                        }
                    }
                }
            }
        }
    }

    VetAppLayout(
        currentScreen = "Consultas",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = tr("Atención de Consultas Clínicas", "Clinical consultations"),
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
                    .background(CardWhite)
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
                            text = tr("Pacientes Registrados", "Registered patients"),
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
                        Text(tr("➕ Vincular por ID Médico (`medical_id`)", "➕ Link by Medical ID (`medical_id`)"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // BÚSQUEDA POR MEDICAL_ID O NOMBRE
                    OutlinedTextField(
                        value = searchMedicalIdQuery,
                        onValueChange = { searchMedicalIdQuery = it },
                        placeholder = { Text(tr("Buscar por ID Médico (PM-XXXX) o Nombre", "Search by Medical ID (PM-XXXX) or name"), fontSize = 12.sp) },
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
                        Text(tr("Cargando pacientes…", "Loading patients…"), fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                    } else if (filtered.isEmpty()) {
                        Text(tr("No se encontraron pacientes.", "No patients found."), fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 12.dp))
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
                                val breedStr = p.breed ?: tr("Mestizo", "Mixed breed")

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

            // PANEL DERECHO: Acciones de Consulta (0.68f)
            Box(
                modifier = Modifier
                    .weight(0.68f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardWhite)
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    val p = selectedPatient
                    if (p != null) {
                        val medId = p.medicalId ?: "PM-${p.id.take(4).uppercase()}"
                        val pName = p.name ?: "Mascota"
                        val pSpecies = p.species ?: "Especie"
                        val pBreed = p.breed ?: "Mestizo"
                        val ownerNameStr = listOfNotNull(selectedOwner?.firstName, selectedOwner?.lastName).joinToString(" ").ifBlank {
                            selectedOwner?.displayName ?: tr("Propietario Registrado", "Registered owner")
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(EmeraldLightBg)
                                .border(2.dp, ClinicalEmerald, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(pName.take(2).uppercase(), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = SelectedText)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(pName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${tr("ID Médico", "Medical ID")}: $medId • $pSpecies • $pBreed", fontSize = 14.sp, color = TextSecondary)
                        Text("${tr("Propietario", "Owner")}: $ownerNameStr", fontSize = 13.sp, color = TextMuted)

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { showCategoryDialog = true },
                            modifier = Modifier.width(320.dp).height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                        ) {
                            Icon(Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tr("Iniciar Nueva Consulta con Plantilla", "Start new consultation with template"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showEditPetDialog = true },
                            modifier = Modifier.width(320.dp).height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr("Editar Datos de la Mascota", "Edit pet details"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                        }
                    } else {
                        Text(tr("Selecciona un paciente a la izquierda para iniciar una consulta.", "Select a patient on the left to start a consultation."), color = TextSecondary, fontSize = 15.sp)
                    }

                    statusMessage?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
