package com.pawsmedic.clinic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pawsmedic.core.di.coreModule
import com.pawsmedic.features.auth.di.authModule
import com.pawsmedic.features.profile.di.profileModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin { modules(coreModule(), authModule, profileModule) }
    Window(onCloseRequest = ::exitApplication, title = "PawMedic - Clinic") {
        ClinicApp()
    }
}

@Composable
private fun ClinicApp() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("PawMedic Clinic")
            Text("Clinical workspace foundation")
        }
    }
}
