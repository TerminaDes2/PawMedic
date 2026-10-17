package com.pawsmedic.mobile.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID

data class GoogleSignInCredentials(
    val idToken: String,
    val rawNonce: String,
)

class GoogleSignInHelper(
    private val context: Context,
    private val serverClientId: String,
) {
    private val credentialManager = CredentialManager.create(context)

    companion object {
        private const val TAG = "GoogleSignInHelper"
    }

    suspend fun signIn(action: String): Result<GoogleSignInCredentials> {
        if (serverClientId.isBlank()) {
            Log.e(TAG, "GOOGLE_WEB_CLIENT_ID está vacío en .env")
            return Result.failure(
                IllegalArgumentException("Configura GOOGLE_WEB_CLIENT_ID en tu archivo .env"),
            )
        }

        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = MessageDigest.getInstance("SHA-256")
            .digest(rawNonce.toByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }

        // GetSignInWithGoogleOption is designed for explicit "Sign in with Google" button clicks.
        // It prompts the Google Account picker bottom sheet for any Google account on the device.
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        return try {
            Log.d(TAG, "Iniciando solicitud GetSignInWithGoogleOption con serverClientId: $serverClientId")
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            Log.d(TAG, "Credencial de Google obtenida exitosamente para el usuario: ${googleIdTokenCredential.id}")
            Result.success(
                GoogleSignInCredentials(
                    idToken = googleIdTokenCredential.idToken,
                    rawNonce = rawNonce,
                ),
            )
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "GetCredentialCancellationException: ${e.message}", e)
            val msg = e.message ?: ""
            val message = when {
                msg.contains("reauth failed", ignoreCase = true) || msg.contains("16") -> {
                    "Google no pudo autenticar la cuenta seleccionada (error 16). Verifica que la cuenta esté activa en el teléfono o intenta con otra cuenta Google."
                }
                msg.contains("developer", ignoreCase = true) || msg.contains("10") -> {
                    "Error de configuración de Google OAuth (error 10). Verifica la huella SHA-1 y el Client ID en Google Cloud Console / Supabase."
                }
                else -> {
                    "$action con Google cancelado. Puedes intentarlo de nuevo."
                }
            }
            Result.failure(IllegalStateException(message, e))
        } catch (e: NoCredentialException) {
            Log.e(TAG, "NoCredentialException al solicitar inicio de sesión con Google: ${e.message}", e)
            return Result.failure(
                IllegalStateException(
                    "No se encontró una cuenta de Google disponible en el teléfono. Agrega una cuenta Google en Ajustes y verifica que Google Play Services esté actualizado.",
                    e,
                ),
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "GoogleIdTokenParsingException: ${e.message}", e)
            Result.failure(IllegalStateException("Google devolvió una credencial no válida", e))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException (${e.type}): ${e.message}", e)
            Result.failure(IllegalStateException("Error en Google Sign-In (${e.type}): ${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado en Google Sign-In: ${e.message}", e)
            Result.failure(IllegalStateException("Error inesperado en Google Sign-In: ${e.message}", e))
        }
    }
}


