import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

apply(plugin = "com.google.gms.google-services")

android {
    namespace = "com.pawsmedic.mobile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    
    defaultConfig { 
        applicationId = "com.pawsmedic.mobile"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        
        val properties = Properties()
        val localPropertiesFile = rootProject.file(".env")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use(properties::load)
        }
        val sharedProperties = Properties()
        rootProject.file("config/client.properties").inputStream().use(sharedProperties::load)

        fun clientConfig(name: String): String =
            sequenceOf(properties.getProperty(name), System.getenv(name), sharedProperties.getProperty(name))
                .mapNotNull { it?.trim()?.trim('"')?.takeIf(String::isNotBlank) }
                .firstOrNull().orEmpty()

        val cleanUrl = clientConfig("SUPABASE_URL")
        val cleanKey = clientConfig("SUPABASE_ANON_KEY")
        val cleanGoogleClientId = clientConfig("GOOGLE_WEB_CLIENT_ID")
        require(cleanUrl.isNotBlank() && cleanKey.isNotBlank()) {
            "Configura SUPABASE_URL y SUPABASE_ANON_KEY en config/client.properties o .env antes de compilar mobile-android."
        }
        fun buildConfigString(value: String) =
            "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

        buildConfigField("String", "SUPABASE_URL", buildConfigString(cleanUrl))
        buildConfigField("String", "SUPABASE_ANON_KEY", buildConfigString(cleanKey))
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", buildConfigString(cleanGoogleClientId))
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    buildFeatures { 
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared:features:auth"))
    implementation(project(":shared:features:pets"))
    implementation(project(":shared:features:appointments"))
    implementation(project(":shared:features:products"))
    implementation(project(":shared:core:design-system"))
    implementation(project(":shared:core:network"))
    implementation(project(":shared:core:model"))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.client.cio)
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.firebase:firebase-messaging:24.1.1")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation(libs.koin.core)
}
