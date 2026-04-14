pluginManagement {
    repositories {
        mavenLocal() // Leaves - local leavesweight snapshot for Paper 26.1 upgrade
        gradlePluginPortal()
        maven("https://repo.leavesmc.org/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

rootProject.name = "Leaves"

include("leaves-api", "leaves-server")
