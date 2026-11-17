package com.pawsmedic.desktop.veterinary.ui

import com.pawsmedic.desktop.admin.translation.translateLocally
import com.pawsmedic.desktop.admin.translation.translateBatchLocally
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocalTranslationTest {
    @Test
    fun rejectsInvalidTextBeforeStartingTheEngine() {
        runBlocking {
            assertFailsWith<IllegalArgumentException> { translateLocally("  ") }
            assertFailsWith<IllegalArgumentException> { translateLocally("x".repeat(4001)) }
            assertFailsWith<IllegalArgumentException> { translateBatchLocally(emptyList()) }
            assertFailsWith<IllegalArgumentException> { translateBatchLocally(List(51) { "texto" }) }
            assertFailsWith<IllegalArgumentException> { translateBatchLocally(listOf("válido", " ")) }
        }
    }

    @Test
    fun translatesThroughRealLocalWorkerWhenConfigured() {
        val python = System.getenv("PAWMEDIC_TEST_ARGOS_PYTHON")
        val executable = System.getenv("PAWMEDIC_TEST_ARGOS_EXECUTABLE")
        val packages = System.getenv("PAWMEDIC_TEST_ARGOS_PACKAGES")
        assumeTrue("Set the Argos engine and model paths to run this integration test", (python != null || executable != null) && packages != null)
        val previousPython = System.getProperty("pawmedic.argos.python")
        val previousExecutable = System.getProperty("pawmedic.argos.executable")
        val previousPackages = System.getProperty("pawmedic.argos.packages")
        try {
            if (executable != null) {
                System.setProperty("pawmedic.argos.executable", executable)
            } else {
                System.clearProperty("pawmedic.argos.executable")
                System.setProperty("pawmedic.argos.python", requireNotNull(python))
            }
            System.setProperty("pawmedic.argos.packages", requireNotNull(packages))
            val result = runBlocking { translateLocally("El perro tiene fiebre.") }
            assertTrue(result.contains("dog", ignoreCase = true))
            assertTrue(result.contains("fever", ignoreCase = true))
            val batch = runBlocking { translateBatchLocally(listOf("El perro tiene fiebre.", "Consulta general")) }
            assertTrue(batch.size == 2)
            assertTrue(batch[0].contains("dog", ignoreCase = true))
            assertTrue(batch[1].contains("consult", ignoreCase = true))
        } finally {
            if (previousPython == null) System.clearProperty("pawmedic.argos.python")
            else System.setProperty("pawmedic.argos.python", previousPython)
            if (previousExecutable == null) System.clearProperty("pawmedic.argos.executable")
            else System.setProperty("pawmedic.argos.executable", previousExecutable)
            if (previousPackages == null) System.clearProperty("pawmedic.argos.packages")
            else System.setProperty("pawmedic.argos.packages", previousPackages)
        }
    }
}
