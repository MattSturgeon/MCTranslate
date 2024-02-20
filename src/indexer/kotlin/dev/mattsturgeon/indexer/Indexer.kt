package dev.mattsturgeon.indexer

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.defaultLazy
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import dev.mattsturgeon.dev.mattsturgeon.minecraft.MinecraftAssetIndex
import dev.mattsturgeon.dev.mattsturgeon.minecraft.AssetObject
import dev.mattsturgeon.extensions.asset
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest

/**
 * JVM Entrypoint, runs [Indexer]
 */
fun main(args: Array<String>) = Indexer().main(args)

/**
 * Hash the file using SHA-1
 */
@OptIn(ExperimentalStdlibApi::class)
private fun File.sha1(): String {
    val digest = MessageDigest.getInstance("SHA-1")
    digest.update(readBytes())
    return digest.digest().toHexString()
}

/**
 * Return the file size via [java.nio][Files.size]
 *
 * @see BasicFileAttributes.size
 */
private fun File.size() = Files.size(toPath())

/**
 * Internal tool intended to aid in manually creating integration tests that use Minecraft's indexed asset directories.
 *
 * Assumes [MinecraftAssetIndex] and [File.asset] are working correctly, so check existing tests pass before use.
 */
class Indexer : CliktCommand() {

    private val json = Json { prettyPrint = true }

    private val input by argument()
        .file(mustExist = true, mustBeReadable = true, canBeFile = false, canBeDir = true)
        .help("Input directory")

    private val output by argument()
        .file(mustExist = false).help("Output directory")
        .defaultLazy { input.resolveSibling(input.name + ".indexed") }

    private val indexName by option("-i", "--index", "--assetIndex")
        .help("Index name")
        .defaultLazy { input.name }

    /**
     * Entrypoint, called by [main]
     */
    override fun run() = writeIndex(indexFiles())

    // Writes the index file to the output directory
    @OptIn(ExperimentalSerializationApi::class)
    private fun writeIndex(index: MinecraftAssetIndex) {
        val indexFile = output
            .resolve("indexes")
            .resolve("$indexName.json")
        indexFile.parentFile.mkdirs()
        json.encodeToStream(index, indexFile.outputStream())
    }

    // Visits the input files,
    // copy their content to the output objects store,
    // finally returns file metadata as a MinecraftAssetIndex
    private fun indexFiles() = MinecraftAssetIndex(input.walk()
        .filterNot { it.isDirectory }
        .filter { it.isFile }
        .associate { file: File ->
            val path = file.relativeTo(input).path
            val size = file.size().toULong()
            val hash = file.sha1()
            val destination = output.asset(hash)
            println("File ${file.path} will be written to ${destination.path}")

            // Copy file and store index entry
            destination.parentFile.mkdirs()
            file.copyTo(destination, overwrite = true)

            // Associate path with object
            path to AssetObject(hash, size)
        })
}
