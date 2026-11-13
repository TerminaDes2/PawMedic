plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.serialization)
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared:features:auth"))
    implementation(project(":shared:core:model"))
    implementation(project(":apps:desktop-admin"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.cio)
    implementation("net.java.dev.jna:jna-platform:5.6.0")
    implementation("com.github.sarxos:webcam-capture:0.3.12")
    implementation("org.slf4j:slf4j-nop:2.0.13")
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    testImplementation(kotlin("test"))
}

application { mainClass.set("com.pawsmedic.desktop.veterinary.MainKt") }

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
