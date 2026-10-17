plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget()
    jvm("desktop")
    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.client.core)
            api(libs.supabase.auth)
            api(libs.supabase.postgrest)
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

android {
    namespace = "com.pawsmedic.shared.core.network"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
}
