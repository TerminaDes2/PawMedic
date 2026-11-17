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
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
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

    suspend fun signIn(language: AppLanguage): Result<GoogleSignInCredentials> {
        if (serverClientId.isBlank()) {
            Log.e(TAG, "GOOGLE_WEB_CLIENT_ID no está configurado")
            return Result.failure(
                IllegalArgumentException(language.text("Configura GOOGLE_WEB_CLIENT_ID en config/client.properties o .env", "Configure GOOGLE_WEB_CLIENT_ID in config/client.properties or .env")),
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
                    language.text("Google no pudo autenticar la cuenta seleccionada (error 16). Verifica que la cuenta esté activa en el teléfono o intenta con otra cuenta Google.", "Google could not authenticate the selected account (error 16). Check that the account is active on the phone or try another Google account.")
                }
                msg.contains("developer", ignoreCase = true) || msg.contains("10") -> {
                    language.text("Error de configuración de Google OAuth (error 10). Verifica la huella SHA-1 y el Client ID en Google Cloud Console / Supabase.", "Google OAuth configuration error (error 10). Check the SHA-1 fingerprint and client ID in Google Cloud Console / Supabase.")
                }
                else -> {
                    language.text("Operación con Google cancelada. Puedes intentarlo de nuevo.", "Google sign-in was cancelled. You can try again.")
                }
            }
            Result.failure(IllegalStateException(message, e))
        } catch (e: NoCredentialException) {
            Log.e(TAG, "NoCredentialException al solicitar inicio de sesión con Google: ${e.message}", e)
            return Result.failure(
                IllegalStateException(
                    language.text("No se encontró una cuenta de Google disponible en el teléfono. Agrega una cuenta Google en Ajustes y verifica que Google Play Services esté actualizado.", "No Google account is available on the phone. Add one in Settings and make sure Google Play Services is up to date."),
                    e,
                ),
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "GoogleIdTokenParsingException: ${e.message}", e)
            Result.failure(IllegalStateException(language.text("Google devolvió una credencial no válida", "Google returned an invalid credential"), e))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException (${e.type}): ${e.message}", e)
            Result.failure(IllegalStateException(language.text("Error en Google Sign-In (${e.type}): ${e.message}", "Google sign-in error (${e.type}): ${e.message}"), e))
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado en Google Sign-In: ${e.message}", e)
            Result.failure(IllegalStateException(language.text("Error inesperado en Google Sign-In: ${e.message}", "Unexpected Google sign-in error: ${e.message}"), e))
        }
    }
}
