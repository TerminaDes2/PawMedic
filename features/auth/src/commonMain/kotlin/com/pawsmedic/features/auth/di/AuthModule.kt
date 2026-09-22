package com.pawsmedic.features.auth.di

import com.pawsmedic.features.auth.presentation.AuthViewModel
import org.koin.dsl.module

val authModule = module {
    factory { AuthViewModel(get()) }
}
