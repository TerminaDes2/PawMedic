package com.pawsmedic.core.network

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SupabaseConnectionTest {

    @Test
    fun testSupabaseConnectionAndQuery() = runTest {
        try {
            // Intentamos realizar una consulta de prueba a la tabla 'profiles'
            val result = supabase.from("profiles").select().decodeList<Map<String, Any>>()
            println("Conexión exitosa a Supabase. Registros obtenidos: ${result.size}")
            assertTrue(true)
        } catch (e: Exception) {
            println("Fallo al conectar con Supabase: ${e.message}")
            throw e
        }
    }
}
