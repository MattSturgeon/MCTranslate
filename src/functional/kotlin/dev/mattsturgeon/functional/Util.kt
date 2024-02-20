package dev.mattsturgeon.functional


import org.gradle.util.GradleVersion
import java.io.File
import java.io.FileNotFoundException

val GRADLE_CURRENT: String = GradleVersion.current().version;

fun cleanBuildDir(project: File) {
    val buildDir = project.resolve("build")
    try {
        buildDir.deleteRecursively()
    } catch (_: FileNotFoundException) {
        // Ignored
    } catch (e: Throwable) {
        System.err.println("Error removing build directory in \"${project.path}\": $e")
    }
}