import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.util.Locale

plugins {
    java
    id("org.leavesmc.leavesweight.patcher") version "2.1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
            //vendor = JvmVendorSpec.ADOPTIUM
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
        options.forkOptions.memoryMaximumSize = "6g"
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

val patchTasks = listOf(
    "::applyPaperApiPatches" to "::rebuildPaperApiPatches",
    "::applyPaperSingleFilePatches" to "::rebuildPaperSingleFilePatches",
    ":leaves-server:applyMinecraftPatches" to ":leaves-server:rebuildMinecraftPatches",
    ":leaves-server:applyPaperServerPatches" to ":leaves-server:rebuildPaperServerPatches"
)
val patchDir = listOf(
    "paper-api",
    "leaves-server",
    "leaves-server/src/minecraft/java",
    "paper-server",
)

val statusFile = layout.buildDirectory.file("patchTaskStatus.json").get().asFile
fun readTaskStatus(): Map<String, String> {
    if (!statusFile.exists()) {
        return emptyMap()
    }
    try {
        @Suppress("UNCHECKED_CAST")
        return JsonSlurper().parse(statusFile) as Map<String, String>
    } catch (_: Exception) {
        return emptyMap()
    }
}

fun writeTaskStatus(status: Map<String, String>) {
    statusFile.writeText(JsonBuilder(status).toPrettyString())
}

fun executeCommand(command: List<String>, directory: File): Boolean {
    try {
        val processBuilder = ProcessBuilder(command)
        processBuilder.directory(directory)

        val process = processBuilder.start()

        val outputThread = Thread {
            process.inputStream.bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    println(line)
                }
            }
        }

        val errorThread = Thread {
            process.errorStream.bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    System.err.println(line)
                }
            }
        }

        outputThread.start()
        errorThread.start()

        val exitCode = process.waitFor()

        outputThread.join()
        errorThread.join()

        return exitCode == 0
    } catch (_: Exception) {
        return false
    }
}

fun hasGitChanges(directory: File): Boolean {
    val process = ProcessBuilder("git", "status", "--porcelain").directory(directory).start()
    val output = process.inputStream.bufferedReader().readText()
    process.waitFor()

    return output.isNotEmpty()
}

fun executeTask(taskPath: String): Boolean {
    val parts = taskPath.split(":")
    val projectPath = if (parts.size > 2) parts.subList(0, parts.size - 1).joinToString(":") else ":"
    val taskName = parts.last()
    val fullTaskPath = if (projectPath == ":" || projectPath.isEmpty()) taskName else "$projectPath:$taskName"

    val gradlew = if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows"))
        "${project.rootDir}\\gradlew.bat"
    else
        "${project.rootDir}/gradlew"

    if (!executeCommand(listOf(gradlew, fullTaskPath), project.projectDir)) {
        throw GradleException("Task $fullTaskPath FAILED")
    }

    return true
}

tasks.register("applyAllPatchesSequentially") {
    group = "leaves"
    description = "Apply all patches sequentially, run rebuild after success, wait for manual fix if failed"

    doFirst {
        println("🚀 Starting sequential patch application...")
    }

    doLast {
        val taskStatus = readTaskStatus().toMutableMap()
        var currentIndex = 0

        while (currentIndex < patchTasks.size) {
            val (applyTaskPath, rebuildTaskPath) = patchTasks[currentIndex]

            if (taskStatus[applyTaskPath] == "COMPLETED") {
                println("⏩ Skipping completed task: $applyTaskPath")
                currentIndex++
                continue
            }

            if (taskStatus[applyTaskPath] == "FAILED") {
                println("⚠️ Detected previous failure of $applyTaskPath, running $rebuildTaskPath first")
                try {
                    executeTask(rebuildTaskPath)
                    println("✅ $rebuildTaskPath completed successfully")
                } catch (e: Exception) {
                    taskStatus[applyTaskPath] = "FAILED"
                    writeTaskStatus(taskStatus)
                    throw GradleException("$rebuildTaskPath failed: ${e.message}", e)
                }
            }

            println("🔄 Running $applyTaskPath...")
            try {
                executeTask(applyTaskPath)
                println("✅ $applyTaskPath completed successfully")

                println("🔄 Running $rebuildTaskPath after successful apply...")
                try {
                    executeTask(rebuildTaskPath)
                    println("✅ $rebuildTaskPath completed successfully")
                } catch (e: Exception) {
                    println("⚠️ $rebuildTaskPath failed, but continuing with next tasks: ${e.message}")
                }

                taskStatus[applyTaskPath] = "COMPLETED"
                writeTaskStatus(taskStatus)
                currentIndex++
            } catch (e: Exception) {
                taskStatus[applyTaskPath] = "FAILED"
                writeTaskStatus(taskStatus)
                throw GradleException("$applyTaskPath failed, please fix the issues and run this task again", e)
            }
        }

        if (currentIndex >= patchTasks.size) {
            tasks.named("resetPatchTaskStatus").get().actions.forEach { it.execute(tasks.named("resetPatchTaskStatus").get()) }
            println("✨ All patch tasks completed successfully!")
        }
    }
}

tasks.register("applyNextPatch") {
    group = "leaves"
    description = "Apply the next patch"

    doLast {
        val taskStatus = readTaskStatus().toMutableMap()

        val failedIndex = patchTasks.indexOfFirst { (applyTaskPath, _) ->
            taskStatus[applyTaskPath] == "FAILED"
        }

        if (failedIndex >= 0) {
            val directory = project.projectDir.resolve(patchDir[failedIndex])
            executeCommand(listOf("git", "add", "."), directory)

            val gitCommand = if (hasGitChanges(directory)) {
                listOf("git", "am", "--continue")
            } else {
                listOf("git", "am", "--skip")
            }

            executeCommand(gitCommand, directory)
        }
    }
}

tasks.register("resetPatchTaskStatus") {
    group = "leaves"
    description = "Reset the status of all patch tasks"

    doLast {
        if (statusFile.exists()) {
            statusFile.delete()
            println("🧹 All patch task statuses have been reset")
        }
    }
}

tasks.register("showPatchTaskStatus") {
    group = "leaves"
    description = "Show the current status of all patch tasks"

    doLast {
        val status = readTaskStatus()
        println("📊 Patch Task Status:")

        if (status.isEmpty()) {
            println("  No tasks executed yet or status has been reset")
        } else {
            patchTasks.forEach { (applyTask, _) ->
                val taskStatus = status[applyTask] ?: "PENDING"
                val statusIcon = when (taskStatus) {
                    "COMPLETED" -> "✅"
                    "FAILED" -> "❌"
                    else -> "⏳"
                }
                println("  $statusIcon $applyTask: $taskStatus")
            }
        }
    }
}
