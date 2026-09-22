package com.pawsmedic.core.data

import com.pawsmedic.core.model.AuthCredentials
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepositoryTest {
    @Test
    fun inMemoryRepositoryCreatesAndClearsSession() = runTest {
        val repository = InMemoryAuthRepository()
        val result = repository.signIn(AuthCredentials("owner@example.test", "password"))

        assertTrue(result.isSuccess)
        assertEquals("owner@example.test", repository.currentSession()?.userId)
        repository.signOut()
        assertEquals(null, repository.currentSession())
    }

    @Test
    fun unconfiguredSupabaseRepositoryFailsClearly() = runTest {
        val result = SupabaseAuthRepository(SupabaseConfig()).signIn(
            AuthCredentials("owner@example.test", "password")
        )
        assertTrue(result.isFailure)
    }
}
