package com.pawsmedic.features.profile.di

import com.pawsmedic.features.profile.presentation.ProfileViewModel
import org.koin.dsl.module

val profileModule = module {
    factory { ProfileViewModel(get()) }
}
