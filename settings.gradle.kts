pluginManagement {
    repositories {
        mavenLocal() // Only for test Leavesweight, should be removed after merge LeavesMC/leavesweight#2
        gradlePluginPortal()
        maven("https://repo.leavesmc.org/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.9.0")
}

rootProject.name = "Leaves"

include("leaves-api", "leaves-server")
