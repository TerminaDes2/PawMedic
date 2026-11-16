package com.pawsmedic.desktop.admin

import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClientConfigurationTest {
    @Test
    fun selectsLocalThenEnvironmentThenSharedValues() {
        assertEquals("local", resolveClientConfiguration(" \"local\" ", "environment", "shared"))
        assertEquals("environment", resolveClientConfiguration(" ", "environment", "shared"))
        assertEquals("shared", resolveClientConfiguration(null, "", "shared"))
        assertNull(resolveClientConfiguration(null, null, null))
    }

    @Test
    fun includesSharedConfigurationInResources() {
        val stream = assertNotNull(javaClass.getResourceAsStream("/client.properties"))
        val properties = stream.use { Properties().apply { load(it) } }
        assertTrue(properties.getProperty("SUPABASE_URL").isNotBlank())
        assertTrue(properties.getProperty("SUPABASE_ANON_KEY").isNotBlank())
    }
}
