plugins {
    kotlin("jvm")
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
        mainClass = "com.pawsmedic.patient.MainKt"
        nativeDistributions {
            packageName = "PawMedicPatient"
            packageVersion = "0.1.0"
        }
    }
}
