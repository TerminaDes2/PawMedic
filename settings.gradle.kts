pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PawMedic"

// The original scaffold is intentionally retained on disk for reference, but
// these are the canonical KMP modules used by the project.
include(
    ":shared:core:common",
    ":shared:core:model",
    ":shared:core:auth",
    ":shared:core:network",
    ":shared:core:storage",
    ":shared:core:design-system",
    ":shared:core:testing",
    ":shared:features:auth",
    ":shared:features:pets",
    ":shared:features:businesses",
    ":shared:features:appointments",
    ":shared:features:services",
    ":shared:features:medical-records",
    ":shared:features:products",
    ":shared:features:reports",
    ":shared:features:platform-admin",
    ":apps:mobile-android",
    ":apps:desktop-veterinary",
    ":apps:desktop-admin"
)
