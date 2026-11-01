package com.pawsmedic.patient

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
    Window(onCloseRequest = ::exitApplication, title = "PawMedic - Patient") {
        PatientApp()
    }
}

@Composable
private fun PatientApp() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("PawMedic Patient")
            Text("Your pets and consultation history")
        }
    }
}
