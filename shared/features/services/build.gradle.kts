plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()
    jvm("desktop")
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:core:model"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.supabase.postgrest)
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

android { namespace = "com.pawsmedic.shared.features.services"; compileSdk = libs.versions.android.compileSdk.get().toInt(); defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() } }
