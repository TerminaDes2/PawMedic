import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.pawsmedic.mobile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    
    defaultConfig { 
        applicationId = "com.pawsmedic.mobile"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        
        // Leer el archivo .env desde la raíz del proyecto
        val properties = Properties()
        val localPropertiesFile = rootProject.file(".env")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use(properties::load)
        }

        val rawUrl = properties.getProperty("SUPABASE_URL") ?: System.getenv("SUPABASE_URL") ?: ""
        val rawKey = properties.getProperty("SUPABASE_ANON_KEY") ?: System.getenv("SUPABASE_ANON_KEY") ?: ""
        val googleClientId = properties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: System.getenv("GOOGLE_WEB_CLIENT_ID") ?: ""

        val cleanUrl = rawUrl.replace("\"", "").trim()
        val cleanKey = rawKey.replace("\"", "").trim()
        val cleanGoogleClientId = googleClientId.replace("\"", "").trim()
        require(cleanUrl.isNotBlank() && cleanKey.isNotBlank()) {
            "Configura SUPABASE_URL y SUPABASE_ANON_KEY en .env antes de compilar mobile-android."
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
    implementation(project(":shared:core:design-system"))
    implementation(project(":shared:core:network"))
    implementation(project(":shared:core:model"))
    implementation(libs.supabase.auth)
    implementation(libs.ktor.client.cio)
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation(libs.koin.core)
}
