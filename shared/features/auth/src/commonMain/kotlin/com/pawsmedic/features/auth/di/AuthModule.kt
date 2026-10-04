package com.pawsmedic.features.auth.di

import com.pawsmedic.features.auth.data.datasource.NoOpSupabaseAuthDataSource
import com.pawsmedic.features.auth.data.datasource.SupabaseAuthDataSource
import com.pawsmedic.features.auth.data.repository.SupabaseAuthRepository
import com.pawsmedic.features.auth.domain.repository.AuthRepository
import com.pawsmedic.features.auth.domain.usecase.LoginUseCase
import com.pawsmedic.features.auth.domain.usecase.RegisterUseCase
import com.pawsmedic.features.auth.domain.usecase.RestoreSessionUseCase
import com.pawsmedic.features.auth.presentation.AuthViewModel
import com.pawsmedic.features.auth.presentation.login.LoginViewModel
import com.pawsmedic.features.auth.presentation.register.RegisterViewModel
import com.pawsmedic.features.auth.presentation.splash.SplashViewModel
import org.koin.dsl.module

fun authModule() = module {
    single<SupabaseAuthDataSource> { NoOpSupabaseAuthDataSource() }
    single<AuthRepository> { SupabaseAuthRepository(get()) }
    factory { RestoreSessionUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { AuthViewModel(get(), get()) }
    factory { SplashViewModel(get()) }
    factory { LoginViewModel(get()) }
    factory { RegisterViewModel(get()) }
}
