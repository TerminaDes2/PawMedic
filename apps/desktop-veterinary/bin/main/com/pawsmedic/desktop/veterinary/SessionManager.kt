package com.pawsmedic.desktop.veterinary

import java.util.prefs.Preferences
import java.util.Base64
import com.sun.jna.Platform
import com.sun.jna.platform.win32.Crypt32Util
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Gestor de Persistencia de Sesión Local para Compose Desktop (JVM).
 * Utiliza Java Preferences API para guardar el estado de "Recordarme" y el correo activo
 * a nivel de usuario en el sistema operativo (Windows Registry, macOS Plist, Linux Prefs).
 */
object SessionManager {
    private val prefs: Preferences = Preferences.userNodeForPackage(SessionManager::class.java)
    private const val KEY_REMEMBER_ME = "pawsmedic_remember_me"
    private const val KEY_SAVED_EMAIL = "pawsmedic_saved_email"

    fun saveSession(email: String, rememberMe: Boolean) {
        prefs.putBoolean(KEY_REMEMBER_ME, rememberMe)
        if (rememberMe && email.isNotBlank()) {
            prefs.put(KEY_SAVED_EMAIL, email.trim())
        } else {
            prefs.remove(KEY_SAVED_EMAIL)
        }
        prefs.flush()
    }

    fun isRememberMeEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun getSavedEmail(): String? {
        if (!isRememberMeEnabled()) return null
        val email = prefs.get(KEY_SAVED_EMAIL, null)
        return if (!email.isNullOrBlank()) email else null
    }

    fun clearSession() {
        prefs.putBoolean(KEY_REMEMBER_ME, false)
        prefs.remove(KEY_SAVED_EMAIL)
        prefs.flush()
    }
}

internal class SecureSessionStore(
    private val prefs: Preferences,
    private val rememberEnabled: () -> Boolean
) : io.github.jan.supabase.auth.SessionManager {
    private val keySession = "pawsmedic_encrypted_session"
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    override suspend fun saveSession(session: UserSession) {
        if (!rememberEnabled()) return
        check(Platform.isWindows()) {
            "Recordar sesión solo está disponible en Windows con almacenamiento protegido."
        }
        val encrypted = Crypt32Util.cryptProtectData(json.encodeToString(session).toByteArray(Charsets.UTF_8))
        prefs.put(keySession, Base64.getEncoder().encodeToString(encrypted))
        prefs.flush()
    }

    override suspend fun loadSession(): UserSession? {
        if (!rememberEnabled()) return null
        val value = prefs.get(keySession, null) ?: return null
        check(Platform.isWindows()) { "No se puede recuperar la sesión protegida en este sistema." }
        val decrypted = Crypt32Util.cryptUnprotectData(Base64.getDecoder().decode(value))
        return json.decodeFromString(decrypted.toString(Charsets.UTF_8))
    }

    override suspend fun deleteSession() {
        prefs.remove(keySession)
        prefs.flush()
    }
}

internal val SecureSessionStorage = SecureSessionStore(
    Preferences.userNodeForPackage(SessionManager::class.java),
    SessionManager::isRememberMeEnabled
)
