plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()
    jvm("desktop")
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:core:design-system"))
            implementation(project(":shared:core:common"))
            implementation(project(":shared:core:model"))
            implementation(project(":shared:features:appointments"))
            implementation(project(":shared:features:businesses"))
            implementation(project(":shared:features:medical-records"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.supabase.postgrest)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.coil.compose)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.pawsmedic.shared.features.pets"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
    buildFeatures { compose = true }
}
