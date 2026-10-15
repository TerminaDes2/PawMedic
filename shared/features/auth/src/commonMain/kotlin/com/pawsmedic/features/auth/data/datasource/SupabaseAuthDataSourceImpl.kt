package com.pawsmedic.features.auth.data.datasource

import com.pawsmedic.features.auth.data.dto.AuthSessionDto
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAuthDataSourceImpl(
    private val supabaseClient: SupabaseClient
) : SupabaseAuthDataSource {

    override suspend fun currentSession(): AuthSessionDto? {
        val session = supabaseClient.auth.currentSessionOrNull()
        return session?.toAuthoritativeDto()
    }

    override suspend fun signIn(email: String, password: String): AuthSessionDto {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val session = supabaseClient.auth.currentSessionOrNull()
            ?: throw IllegalStateException("Correo o contraseña incorrectos")
        return session.toAuthoritativeDto()
    }

    override suspend fun signUp(
        email: String,
        password: String,
        role: PawMedicRole,
        fullName: String?,
        phone: String?
    ): SignUpResultDto {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("role", role.name)
                fullName?.let { put("full_name", it) }
                phone?.let { put("phone", it) }
            }
        }
        val session = supabaseClient.auth.currentSessionOrNull()
        if (session != null) {
            supabaseClient.auth.signOut()
            throw IllegalStateException(
                "Supabase tiene desactivada la confirmación de correo. Actívala para requerir el código de registro."
            )
        }
        return SignUpResultDto(isVerified = false, session = null)
    }

    override suspend fun verifyEmailOtp(email: String, code: String): AuthSessionDto {
        supabaseClient.auth.verifyEmailOtp(
            type = OtpType.Email.SIGNUP,
            email = email,
            token = code.trim()
        )
        val session = supabaseClient.auth.currentSessionOrNull()
            ?: throw IllegalStateException("El código de verificación es incorrecto o ha expirado")
        return session.toAuthoritativeDto()
    }

    override suspend fun resendEmailOtp(email: String) {
        supabaseClient.auth.resendEmail(
            type = OtpType.Email.SIGNUP,
            email = email
        )
    }

    override suspend fun signInWithGoogleIdToken(idToken: String, rawNonce: String?): AuthSessionDto {
        supabaseClient.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
            if (rawNonce != null) {
                this.nonce = rawNonce
            }
        }
        val session = supabaseClient.auth.currentSessionOrNull()
            ?: throw IllegalStateException("No se pudo iniciar sesión con Google")
        return session.toAuthoritativeDto()
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    private suspend fun UserSession.toAuthoritativeDto(): AuthSessionDto {
        val userId = user?.id?.takeIf(String::isNotBlank)
            ?: throw IllegalStateException("La sesión no contiene un usuario autenticado")
        val profile = supabaseClient.postgrest["profiles"]
            .select(columns = Columns.list("role", "tenant_id")) {
                filter { eq("id", userId) }
            }
            .decodeSingle<ProfileRoleRecord>()

        return AuthSessionDto(
            accessToken = accessToken,
            userId = userId,
            email = user?.email,
            role = PawMedicRole.valueOf(profile.role),
            tenantId = profile.tenantId
        )
    }
}

@Serializable
private data class ProfileRoleRecord(
    val role: String,
    @SerialName("tenant_id") val tenantId: String? = null
)
