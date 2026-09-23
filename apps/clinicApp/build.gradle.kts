plugins {
    kotlin("jvm")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":features:auth"))
    implementation(project(":features:profile"))
    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation("io.insert-koin:koin-core:4.0.0")
}

compose.desktop {
    application {
        mainClass = "com.pawsmedic.clinic.MainKt"
        nativeDistributions {
            packageName = "PawMedicClinic"
            packageVersion = "0.1.0"
        }
    }
}
