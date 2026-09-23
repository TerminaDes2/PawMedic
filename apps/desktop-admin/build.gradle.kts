plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    application
}

dependencies {
    implementation(project(":shared:features:auth"))
    implementation(project(":shared:features:platform-admin"))
    implementation(compose.desktop.currentOs)
}

application { mainClass.set("com.pawsmedic.desktop.admin.MainKt") }
