package dev.mattsturgeon.assets

import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

// These extension functions make getLang() much more readable
private fun File.childFiles() = listFiles { file -> file.isFile } ?: emptyArray()
private fun File.childDirectories() = listFiles { file -> file.isDirectory } ?: emptyArray()

class DirAssets(private val assetsDir: File) : Assets {

    init {
        if (!assetsDir.isDirectory) {
            throw IllegalArgumentException("assetsDir is not a directory")
        }
    }

    override fun packMeta() = try {
        Json.decodeFromString<PackMeta>(assetsDir.resolve("pack.mcmeta").readText())
    } catch (e: FileNotFoundException) {
        null
    }

    override fun getLang(lang: String): Map<String, String>? {
        return assetsDir.childDirectories()
            .asSequence()
            // Stream everything matching /*/lang that is a directory
            .map { it.resolve("lang") }
            .filter { it.isDirectory }
            // Stream all lang files that match the requested lang
            .flatMap { it.childFiles().asSequence() }
            .filter { it.nameWithoutExtension == lang }
            // Parse and combine the lang file
            .map { Language.parse(it.name, it.reader()) }
            .map { it.translations }
            .reduceOrNull(Map<String, String>::plus)
    }
}