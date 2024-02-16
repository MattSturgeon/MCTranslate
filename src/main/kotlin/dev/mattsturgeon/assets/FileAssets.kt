package dev.mattsturgeon.assets

import java.io.File
import java.io.FileNotFoundException
import java.io.Reader
import java.util.function.Supplier

// These extension functions make getLang() much more readable
private fun File.childFiles() = listFiles { file -> file.isFile } ?: emptyArray()
private fun File.childDirectories() = listFiles { file -> file.isDirectory } ?: emptyArray()

internal class FileAssets(private val assetsDir: File) : BaseAssets {

    init {
        if (!assetsDir.isDirectory) {
            throw IllegalArgumentException("assetsDir is not a directory")
        }
    }

    override fun getPackMetaFile() = try {
        assetsDir.resolve("pack.mcmeta").reader()
    } catch (e: FileNotFoundException) {
        null
    }

    override fun getLangFiles(): Iterable<Pair<String, Supplier<Reader>>> {
        return assetsDir.childDirectories()
            .asSequence()
            .map { it.resolve("lang") }
            .filter { it.isDirectory }
            .flatMap { it.childFiles().asSequence() }
            .map { it.name to Supplier<Reader> { it.reader() } }
            .asIterable()
    }
}