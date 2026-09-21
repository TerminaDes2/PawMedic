package com.pawsmedic.core.di

import com.pawsmedic.core.data.AuthRepository
import com.pawsmedic.core.data.InMemoryProfileRepository
import com.pawsmedic.core.data.ProfileRepository
import com.pawsmedic.core.data.SupabaseAuthRepository
import com.pawsmedic.core.data.SupabaseConfig
import org.koin.dsl.module

fun coreModule(config: SupabaseConfig = SupabaseConfig()) = module {
    single { config }
    single<AuthRepository> { SupabaseAuthRepository(get()) }
    single<ProfileRepository> { InMemoryProfileRepository() }
}
