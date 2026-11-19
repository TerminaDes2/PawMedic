import java.net.URI
import java.util.Base64
import java.util.Properties

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
    implementation(project(":shared:features:medical-records"))
    implementation(project(":shared:core:common"))
    implementation(project(":shared:core:model"))
    implementation(project(":apps:desktop-admin"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.cio)
    implementation("net.java.dev.jna:jna-platform:5.6.0")
    implementation("com.github.sarxos:webcam-capture:0.3.12")
    implementation("org.apache.pdfbox:pdfbox:2.0.32")
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.slf4j:slf4j-nop:2.0.13")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.pawsmedic.desktop.veterinary.MainKt")
}

tasks.processResources {
    from(rootProject.file("t_rminos_y_condiciones_y_pol_tica_de_privacidad_pawsmedic.md")) {
        into("legal")
        rename { "terminos-y-privacidad.md" }
    }
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    val argos = layout.buildDirectory
    if (argos.file("argos-dist/pawmedic-argos-worker/pawmedic-argos-worker.exe").get().asFile.isFile) {
        systemProperty("pawmedic.argos.executable", argos.file("argos-dist/pawmedic-argos-worker/pawmedic-argos-worker.exe").get().asFile.absolutePath)
        systemProperty("pawmedic.argos.packages", argos.dir("argos-models").get().asFile.absolutePath)
    }
}

tasks.register<Sync>("installWithTranslation") {
    dependsOn("installDist")
    val argos = layout.buildDirectory
    doFirst {
        check(argos.file("argos-dist/pawmedic-argos-worker/pawmedic-argos-worker.exe").get().asFile.isFile &&
            argos.file("argos-models/translate-es_en-1_9/metadata.json").get().asFile.isFile) {
            "Run python apps\\desktop-veterinary\\translation\\prepare_argos.py first."
        }
    }
    from(argos.dir("install/${project.name}"))
    from(argos.dir("argos-dist/pawmedic-argos-worker")) { into("argos/worker") }
    from(argos.dir("argos-models")) { into("argos/models") }
    into(argos.dir("install-with-translation/${project.name}"))
}

val portableConfig = layout.buildDirectory.dir("portable-config")
val windowsLauncher = layout.buildDirectory.file("windows-launcher/Iniciar PawMedic.exe")
val buildWindowsLauncher = tasks.register<Exec>("buildWindowsLauncher") {
    val source = layout.projectDirectory.file("windows-launcher/PawMedicLauncher.cs")
    inputs.file(source)
    outputs.file(windowsLauncher)
    doFirst {
        val compiler = file("${System.getenv("SystemRoot")}\\Microsoft.NET\\Framework\\v4.0.30319\\csc.exe")
        check(compiler.isFile) { "Windows .NET Framework C# compiler not found." }
        windowsLauncher.get().asFile.parentFile.mkdirs()
        commandLine(
            compiler.absolutePath, "/nologo", "/target:winexe", "/platform:anycpu",
            "/reference:System.Windows.Forms.dll", "/out:${windowsLauncher.get().asFile.absolutePath}",
            source.asFile.absolutePath
        )
    }
}
val preparePortableConfig = tasks.register("preparePortableConfig") {
    outputs.dir(portableConfig)
    outputs.upToDateWhen { false }
    doLast {
        val envFile = rootProject.file(".env")
        val properties = Properties()
        if (envFile.isFile) envFile.inputStream().use(properties::load)
        fun value(name: String): String =
            (properties.getProperty(name)?.trim()?.trim('"')?.takeIf { it.isNotBlank() }
                ?: System.getenv(name)?.trim()?.takeIf { it.isNotBlank() })
                ?: error("Missing $name in .env or environment.")
        val url = value("SUPABASE_URL")
        val key = value("SUPABASE_ANON_KEY")
        check(URI(url).scheme == "https") { "Portable distribution requires an HTTPS Supabase URL." }
        val publicKey = key.startsWith("sb_publishable_") || runCatching {
            val payload = String(Base64.getUrlDecoder().decode(key.split('.')[1]), Charsets.UTF_8)
            payload.contains(Regex("\"role\"\\s*:\\s*\"anon\""))
        }.getOrDefault(false)
        check(publicKey) { "SUPABASE_ANON_KEY must be a public publishable or anon key; never package a secret key." }
        val target = portableConfig.get().file(".env").asFile
        target.parentFile.mkdirs()
        target.writeText("SUPABASE_URL=$url\nSUPABASE_ANON_KEY=$key\n", Charsets.UTF_8)
    }
}

tasks.register<Zip>("portableWindowsZip") {
    dependsOn("installWithTranslation", preparePortableConfig, buildWindowsLauncher)
    val runtime = providers.environmentVariable("JAVA_HOME").map { file(it) }
    doFirst {
        check(runtime.isPresent && runtime.get().resolve("bin/javaw.exe").isFile) {
            "Set JAVA_HOME to a Windows Java runtime before building the portable ZIP."
        }
    }
    archiveFileName.set("PawMedic-Windows-Portable.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    into("PawMedic") {
        from(layout.buildDirectory.dir("install-with-translation/${project.name}"))
        from(runtime) { into("runtime") }
        from(portableConfig)
        from(windowsLauncher)
    }
}
