package com.pawsmedic.features.auth.data.datasource

import com.pawsmedic.features.auth.data.dto.AuthSessionDto
import com.pawsmedic.features.auth.data.dto.toDto
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.domain.model.RegisterParams
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAuthDataSourceImpl(
    private val supabaseClient: SupabaseClient
) : SupabaseAuthDataSource {

    override suspend fun currentSession(): AuthSessionDto? {
        val session = supabaseClient.auth.currentSessionOrNull()
        return session?.toDto()
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): AuthSessionDto {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        val session = supabaseClient.auth.currentSessionOrNull()
            ?: throw IllegalStateException(
                "No se pudo obtener la sesión tras iniciar sesión"
            )

        return session.toDto()
    }

    override suspend fun signUp(
        email: String,
        password: String,
        role: PawMedicRole,
        fullName: String?
    ): AuthSessionDto {
        return signUp(
            RegisterParams(
                fullName = fullName.orEmpty(),
                email = email,
                phone = "",
                password = password,
                role = role
            )
        )
    }

    override suspend fun signUp(params: RegisterParams): AuthSessionDto {
        val nameParts = params.fullName
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = nameParts.drop(1).joinToString(" ")
        val cleanFullName = params.fullName.trim()

        supabaseClient.auth.signUpWith(Email) {
            email = params.email
            password = params.password

            data = buildJsonObject {
                put("role", params.role.name)
                put("full_name", cleanFullName)
                put("nombre", firstName)
                put("apellidos", lastName)
                put("num_tel", params.phone.trim())
            }
        }

        val session = supabaseClient.auth.currentSessionOrNull()
            ?: throw IllegalStateException(
                "No se pudo obtener la sesión tras el registro. " +
                        "Si tienes confirmación de correo activada, confirma el correo e inicia sesión."
            )

        return session.toDto()
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }
}