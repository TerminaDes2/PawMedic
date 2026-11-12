package com.pawsmedic.desktop.veterinary

import java.util.prefs.Preferences

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
        try {
            prefs.flush()
        } catch (e: Exception) {
            // Ignorar errores de flush
        }
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
        try {
            prefs.flush()
        } catch (e: Exception) {
            // Ignorar errores de flush
        }
    }
}
