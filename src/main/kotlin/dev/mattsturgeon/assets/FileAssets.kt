package dev.mattsturgeon.assets

import dev.mattsturgeon.extensions.childDirectories
import dev.mattsturgeon.extensions.childFiles
import java.io.File
import java.io.FileNotFoundException

internal class FileAssets(private val assetsDir: File) : BaseAssets() {

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

    override fun getLangFiles(): Iterable<NamedSupplier> {
        return assetsDir.childDirectories()
            .asSequence()
            .map { it.resolve("lang") }
            .filter { it.isDirectory }
            .flatMap { it.childFiles().asSequence() }
            .map { it.name to { it.reader() } }
            .asIterable()
    }
}