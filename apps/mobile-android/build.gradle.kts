plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.pawsmedic.mobile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { applicationId = "com.pawsmedic.mobile"; minSdk = libs.versions.android.minSdk.get().toInt(); targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":shared:features:auth"))
    implementation("androidx.activity:activity-compose:1.9.3")
}
