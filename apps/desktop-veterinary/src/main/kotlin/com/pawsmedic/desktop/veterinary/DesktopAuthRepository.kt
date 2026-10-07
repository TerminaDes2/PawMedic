package com.pawsmedic.desktop.veterinary

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class BusinessRegistration(
    val clinicName: String,
    val taxId: String,
    val email: String,
    val phone: String,
    val address: String,
    val municipality: String,
    val veterinarianName: String,
    val licenseNumber: String,
    val password: String
)

@Serializable
data class BusinessApplicationRecord(
    @SerialName("id") val id: String,
    @SerialName("applicant_id") val applicantId: String,
    @SerialName("nombre_negocio") val businessName: String,
    @SerialName("domicilio") val address: String,
    @SerialName("municipio") val municipality: String,
    @SerialName("estado") val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("correo_contacto") val contactEmail: String = "",
    @SerialName("ruc") val taxId: String = "",
    @SerialName("telefono_contacto") val phone: String = "",
    @SerialName("responsable_veterinario") val veterinarianName: String = "",
    @SerialName("cedula_profesional") val licenseNumber: String = ""
)

sealed interface DesktopAccess {
    data object Administrator : DesktopAccess
    data class ApprovedVeterinary(val userId: String) : DesktopAccess
    data class PendingVeterinary(
        val userId: String,
        val application: BusinessApplicationRecord
    ) : DesktopAccess
    data class RejectedVeterinary(
        val userId: String,
        val application: BusinessApplicationRecord
    ) : DesktopAccess
}

@Serializable
private data class ProfileRoleRecord(
    @SerialName("role") val role: String
)

class DesktopAuthRepository(
    private val supabaseClient: SupabaseClient
) {
    suspend fun registerBusiness(registration: BusinessRegistration): DesktopAccess? {
        val nameParts = registration.veterinarianName
            .trim()
            .split(Regex("\\s+"))
            .filter(String::isNotBlank)

        supabaseClient.auth.signUpWith(Email) {
            email = registration.email.trim()
            password = registration.password
            data = buildJsonObject {
                put("registration_type", "VETERINARY_BUSINESS")
                put("full_name", registration.veterinarianName.trim())
                put("nombre", nameParts.firstOrNull().orEmpty())
                put("apellidos", nameParts.drop(1).joinToString(" "))
                put("num_tel", registration.phone.trim())
                put("clinic_name", registration.clinicName.trim())
                put("tax_id", registration.taxId.trim())
                put("contact_phone", registration.phone.trim())
                put("clinic_address", registration.address.trim())
                put("municipality", registration.municipality.trim())
                put("veterinarian_name", registration.veterinarianName.trim())
                put("license_number", registration.licenseNumber.trim())
            }
        }

        return if (supabaseClient.auth.currentUserOrNull() != null) {
            resolveCurrentAccess()
        } else {
            null
        }
    }

    suspend fun signIn(email: String, password: String): DesktopAccess {
        supabaseClient.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }

        return try {
            resolveCurrentAccess()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            supabaseClient.auth.signOut()
            throw error
        }
    }

    suspend fun refreshAccess(userId: String): DesktopAccess {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("La sesión expiró. Inicia sesión nuevamente.")
        require(currentUserId == userId) { "La sesión cambió; vuelve a iniciar sesión." }
        return resolveCurrentAccess()
    }

    suspend fun restoreAccess(): DesktopAccess = resolveCurrentAccess()

    suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    private suspend fun resolveCurrentAccess(): DesktopAccess {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Supabase no devolvió un usuario autenticado.")

        val profile = supabaseClient
            .from("profiles")
            .select(columns = Columns.list("role")) {
                filter { eq("id", userId) }
            }
            .decodeSingle<ProfileRoleRecord>()

        if (profile.role == "SUPERADMIN") {
            return DesktopAccess.Administrator
        }

        val applications = supabaseClient
            .from("business_applications")
            .select {
                filter { eq("applicant_id", userId) }
            }
            .decodeList<BusinessApplicationRecord>()

        val application = applications.maxByOrNull { it.createdAt.orEmpty() }
            ?: throw IllegalArgumentException(
                "Esta cuenta no tiene una solicitud de veterinaria registrada."
            )

        return when (application.status) {
            "PENDIENTE" -> DesktopAccess.PendingVeterinary(userId, application)
            "RECHAZADA" -> DesktopAccess.RejectedVeterinary(userId, application)
            "APROBADA" -> {
                if (profile.role != "VETERINARY_BUSINESS") {
                    throw IllegalStateException(
                        "La solicitud está aprobada, pero el perfil no tiene el rol veterinario. Contacta al Superadmin."
                    )
                }
                DesktopAccess.ApprovedVeterinary(userId)
            }
            else -> throw IllegalStateException(
                "La solicitud tiene un estado no reconocido: ${application.status}"
            )
        }
    }
}
