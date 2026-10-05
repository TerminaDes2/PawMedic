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

        val supabaseUrl = properties.getProperty("SUPABASE_URL") ?: System.getenv("SUPABASE_URL") ?: "https://fmcsdyupvmdjvlbzjgml.supabase.co"
        val supabaseAnonKey = properties.getProperty("SUPABASE_ANON_KEY") ?: System.getenv("SUPABASE_ANON_KEY") ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZtY3NkeXVwdm1kanZsYnpqZ21sIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAyOTE0MDMsImV4cCI6MjEwNTg2NzQwM30.y4dIbAKuGdFujQNos3HXGH-TYbtCMMEFw083SbGwkVc"

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
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
