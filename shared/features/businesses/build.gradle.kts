plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()
    jvm("desktop")
    sourceSets {
        commonMain.dependencies { implementation(libs.kotlinx.serialization.json) }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

android { namespace = "com.pawsmedic.shared.features.businesses"; compileSdk = libs.versions.android.compileSdk.get().toInt(); defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() } }
