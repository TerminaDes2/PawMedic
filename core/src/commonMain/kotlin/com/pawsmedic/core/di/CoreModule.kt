package com.pawsmedic.core.di

import com.pawsmedic.core.data.AuthRepository
import com.pawsmedic.core.data.InMemoryProfileRepository
import com.pawsmedic.core.data.ProfileRepository
import com.pawsmedic.core.data.SupabaseAuthRepository
import com.pawsmedic.core.data.SupabaseConfig
import com.pawsmedic.core.network.provideSupabaseClient
import io.github.jan.supabase.SupabaseClient
import org.koin.dsl.module

fun coreModule(config: SupabaseConfig = SupabaseConfig()) = module {
    single { config }

    // Provee la instancia singleton de SupabaseClient con Auth y Postgrest instalados
    single<SupabaseClient> {
        val supabaseConfig = get<SupabaseConfig>()
        provideSupabaseClient(
            url = supabaseConfig.url,        // Verifica si en tu SupabaseConfig se llama .url o .supabaseUrl
            anonKey = supabaseConfig.anonKey  // Verifica si se llama .anonKey o .supabaseKey
        )
    }

    single<AuthRepository> { SupabaseAuthRepository(get()) }
    single<ProfileRepository> { InMemoryProfileRepository() }
}