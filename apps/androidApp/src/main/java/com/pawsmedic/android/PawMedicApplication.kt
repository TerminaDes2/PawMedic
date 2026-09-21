package com.pawsmedic.android

import android.app.Application
import com.pawsmedic.core.di.coreModule
import com.pawsmedic.features.auth.di.authModule
import com.pawsmedic.features.profile.di.profileModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PawMedicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PawMedicApplication)
            modules(coreModule(), authModule, profileModule)
        }
    }
}
