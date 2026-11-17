package com.pawsmedic.mobile

import android.app.Application
import com.pawsmedic.core.network.provideSupabaseClient
import io.github.jan.supabase.SupabaseClient

class PawMedicApplication : Application() {
    val supabaseClient: SupabaseClient by lazy {
        // The SDK otherwise resets session initialization when the app backgrounds.
        provideSupabaseClient(
            BuildConfig.SUPABASE_URL,
            BuildConfig.SUPABASE_ANON_KEY,
            enableAuthLifecycleCallbacks = false,
        )
    }
}
