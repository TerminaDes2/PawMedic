plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()
    jvm("desktop")
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            implementation(libs.supabase.storage)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android { namespace = "com.pawsmedic.shared.core.common"; compileSdk = libs.versions.android.compileSdk.get().toInt(); defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() } }
