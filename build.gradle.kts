plugins {
    java
    id("org.leavesmc.leavesweight.patcher") version "2.0.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.leavesmc.org/releases") {
            content { onlyForConfigurations("leavesclip") }
        }
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
        options.isFork = true
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }


    extensions.configure<PublishingExtension> {
        repositories {
            maven("https://repo.leavesmc.org/snapshots") {
                name = "leaves"
                credentials(PasswordCredentials::class) {
                    username = System.getenv("LEAVES_USERNAME")
                    password = System.getenv("LEAVES_PASSWORD")
                }
            }
        }
    }
}

paperweight {
    upstreams.paper {
        ref = providers.gradleProperty("paperRef")

        patchFile {
            path = "paper-server/build.gradle.kts"
            outputFile = file("leaves-server/build.gradle.kts")
            patchFile = file("leaves-server/build.gradle.kts.patch")
        }
        patchFile {
            path = "paper-api/build.gradle.kts"
            outputFile = file("leaves-api/build.gradle.kts")
            patchFile = file("leaves-api/build.gradle.kts.patch")
        }
        patchDir("paperApi") {
            upstreamPath = "paper-api"
            excludes = setOf("build.gradle.kts")
            patchesDir = file("leaves-api/paper-patches")
            outputDir = file("paper-api")
        }
    }
}