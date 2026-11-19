package com.pawsmedic.desktop.veterinary

import com.sun.jna.Platform
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.runBlocking
import java.util.UUID
import java.util.prefs.Preferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class SecureSessionStoreTest {
    @Test
    fun `remembered session is encrypted restored and removed`() = runBlocking {
        if (!Platform.isWindows()) return@runBlocking
        val prefs = Preferences.userRoot().node("pawsmedic-test-${UUID.randomUUID()}")
        try {
            var remember = true
            val store = SecureSessionStore(prefs) { remember }
            val session = UserSession(accessToken = "test-access-token", refreshToken = "test-refresh-token", expiresIn = 3600, tokenType = "bearer")
            store.saveSession(session)
            val stored = prefs.get("pawsmedic_encrypted_session", "")
            assertFalse(stored.contains("test-access-token"))
            assertEquals(session, store.loadSession())
            remember = false
            assertNull(store.loadSession())
            store.deleteSession()
            remember = true
            assertNull(store.loadSession())
            remember = false
            store.saveSession(session)
            remember = true
            assertNull(store.loadSession())
        } finally {
            prefs.removeNode()
        }
    }
}
