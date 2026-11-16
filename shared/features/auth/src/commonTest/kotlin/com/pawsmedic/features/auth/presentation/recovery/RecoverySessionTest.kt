package com.pawsmedic.features.auth.presentation.recovery

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalSettings
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class RecoverySessionTest {
    @Test
    fun recoverySessionIsIsolatedAndNotPersisted() = runTest {
        val main = createSupabaseClient("https://example.com", "test-key") {
            install(Auth) { minimalSettings() }
        }
        val recovery = createRecoveryClient(main)
        try {
            assertFalse(recovery.auth.config.autoLoadFromStorage)
            assertFalse(recovery.auth.config.autoSaveToStorage)
            main.auth.importAuthToken("main-token")
            recovery.auth.importAuthToken("recovery-token")
            assertEquals("main-token", main.auth.currentSessionOrNull()?.accessToken)
            assertEquals("recovery-token", recovery.auth.currentSessionOrNull()?.accessToken)
            assertNull(recovery.auth.config.sessionManager?.loadSession())
            recovery.auth.clearSession()
            assertNull(recovery.auth.currentSessionOrNull())
            assertEquals("main-token", main.auth.currentSessionOrNull()?.accessToken)
        } finally {
            recovery.close()
            main.close()
        }
    }
}
