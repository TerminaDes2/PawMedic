package com.pawsmedic.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember

// Importaciones de Auth (Existentes)
import com.pawsmedic.features.auth.data.datasource.SupabaseAuthDataSourceImpl
import com.pawsmedic.features.auth.data.repository.SupabaseAuthRepository
import com.pawsmedic.features.auth.domain.usecase.LoginUseCase
import com.pawsmedic.features.auth.domain.usecase.RegisterUseCase
import com.pawsmedic.features.auth.domain.usecase.RestoreSessionUseCase
import com.pawsmedic.features.auth.presentation.login.LoginViewModel
import com.pawsmedic.features.auth.presentation.navigation.AuthNavHost
import com.pawsmedic.features.auth.presentation.register.RegisterViewModel
import com.pawsmedic.features.auth.presentation.splash.SplashViewModel
import com.pawsmedic.shared.core.designsystem.theme.PawMedicTheme

// Importaciones de Appointments / Citas (Nuevas)
import com.pawsmedic.features.appointments.data.datasource.SupabaseAppointmentDataSource
import com.pawsmedic.features.appointments.data.repository.DefaultAppointmentRepository
import com.pawsmedic.features.appointments.presentation.booking.BookingViewModel

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Supabase con Auth y Postgrest
        val supabaseClient = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }

        setContent {
            // =========================================================
            // 1. INICIALIZACIÓN Y DEPENDENCIAS DE AUTENTICACIÓN (INTACTAS)
            // =========================================================
            val dataSource = remember { SupabaseAuthDataSourceImpl(supabaseClient) }
            val repository = remember { SupabaseAuthRepository(dataSource) }
            val restoreSessionUseCase = remember { RestoreSessionUseCase(repository) }
            val loginUseCase = remember { LoginUseCase(repository) }
            val registerUseCase = remember { RegisterUseCase(repository) }

            val splashViewModel = remember { SplashViewModel(restoreSessionUseCase) }
            val loginViewModel = remember { LoginViewModel(loginUseCase) }
            val registerViewModel = remember { RegisterViewModel(registerUseCase) }

            // =========================================================
            // 2. DEPENDENCIAS DE CITAS / APPOINTMENTS (AGREGADAS)
            // =========================================================
            val appointmentDataSource = remember { SupabaseAppointmentDataSource(supabaseClient) }
            val appointmentRepository = remember { DefaultAppointmentRepository(appointmentDataSource) }
            val bookingViewModel = remember { BookingViewModel(appointmentRepository) }

            // =========================================================
            // 3. INTERFAZ GRÁFICA
            // =========================================================
            PawMedicTheme {
                AuthNavHost(
                    splashViewModel = splashViewModel,
                    loginViewModel = loginViewModel,
                    registerViewModel = registerViewModel,
                    supabaseClient = supabaseClient
                )
            }
        }
    }
}
