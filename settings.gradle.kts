pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Rooming"

include(
    ":app",
    ":core:common",
    ":core:navigation",
    ":core:ui",
    ":domain:model",
    ":domain:repository",
    ":domain:usecase",
    ":data:rooms",
    ":data:favorites",
    ":data:bookings",
    ":feature:rooms:api",
    ":feature:rooms:impl",
    ":feature:favorites:api",
    ":feature:favorites:impl",
    ":feature:bookings:api",
    ":feature:bookings:impl",
    ":quality:architecture-test",
)
