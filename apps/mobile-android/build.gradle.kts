import java.util.Properties
import java.io.FileInputStream

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
            properties.load(FileInputStream(localPropertiesFile))
        }

        val rawUrl = properties.getProperty("SUPABASE_URL") ?: System.getenv("SUPABASE_URL") ?: "https://fmcsdyupvmdjvlbzjgml.supabase.co"
        val rawKey = properties.getProperty("SUPABASE_ANON_KEY") ?: System.getenv("SUPABASE_ANON_KEY") ?: "sb_publishable_rO8I1yDID5nKSASNgaa8sA_kZHqICuG"

        val cleanUrl = rawUrl.replace("\"", "").trim()
        val cleanKey = rawKey.replace("\"", "").trim()

        buildConfigField("String", "SUPABASE_URL", "\"$cleanUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$cleanKey\"")
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
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(libs.koin.core)
}
