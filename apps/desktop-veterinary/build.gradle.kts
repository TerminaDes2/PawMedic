plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    application
}

dependencies {
    implementation(project(":shared:features:auth"))
    implementation(project(":apps:desktop-admin"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
}

application { mainClass.set("com.pawsmedic.desktop.veterinary.MainKt") }
