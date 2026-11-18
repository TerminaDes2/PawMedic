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
    implementation(project(":shared:features:platform-admin"))
    implementation(project(":shared:core:model"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.ktor.client.cio)
    implementation("org.apache.pdfbox:pdfbox:2.0.32")
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
}

application { mainClass.set("com.pawsmedic.desktop.admin.MainKt") }

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    val argos = rootProject.layout.projectDirectory.dir("apps/desktop-veterinary/build")
    if (argos.file("argos-dist/pawmedic-argos-worker/pawmedic-argos-worker.exe").asFile.isFile) {
        systemProperty("pawmedic.argos.executable", argos.file("argos-dist/pawmedic-argos-worker/pawmedic-argos-worker.exe").asFile.absolutePath)
        systemProperty("pawmedic.argos.packages", argos.dir("argos-models").asFile.absolutePath)
    }
}

distributions {
    main {
        contents {
            eachFile {
                if (name == "library-desktop-1.7.3.jar" && file.path.contains("components-ui-tooling-preview-desktop")) {
                    name = "compose-preview-library-1.7.3.jar"
                }
            }
        }
    }
}

tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        windowsScript.writeText(windowsScript.readText().replace(Regex("(?m)^set CLASSPATH=.*$")) {
            "set CLASSPATH=%APP_HOME%\\lib\\*"
        })
        unixScript.writeText(unixScript.readText().replace(Regex("(?m)^CLASSPATH=.*$")) {
            "CLASSPATH=\$APP_HOME/lib/*"
        })
    }
}

tasks.register<Sync>("installWithTranslation") {
    dependsOn("installDist")
    val argos = rootProject.layout.projectDirectory.dir("apps/desktop-veterinary/build")
    doFirst {
        check(argos.file("argos-dist/pawmedic-argos-worker/pawmedic-argos-worker.exe").asFile.isFile &&
            argos.file("argos-models/translate-es_en-1_9/metadata.json").asFile.isFile) {
            "Run python apps\\desktop-veterinary\\translation\\prepare_argos.py first."
        }
    }
    from(layout.buildDirectory.dir("install/${project.name}"))
    from(argos.dir("argos-dist/pawmedic-argos-worker")) { into("argos/worker") }
    from(argos.dir("argos-models")) { into("argos/models") }
    into(layout.buildDirectory.dir("install-with-translation/${project.name}"))
}
