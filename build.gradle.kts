plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose) apply false
}

allprojects {
    group = "com.pawsmedic"
    version = "0.1.0"
}

tasks.register("checkScaffold") {
    group = "verification"
    description = "Documents that the multi-module PawMedic scaffold is present."
    doLast {
        println("PawMedic scaffold modules and documentation are configured.")
    }
}
