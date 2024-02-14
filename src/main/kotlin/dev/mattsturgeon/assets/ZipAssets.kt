package dev.mattsturgeon.assets

import dev.mattsturgeon.extensions.basename
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.util.zip.ZipFile

class ZipAssets(val archive: ZipFile) : Assets {
    constructor(file: File) : this(ZipFile(file))

    @OptIn(ExperimentalSerializationApi::class)
    override fun packMeta(): PackMeta? {
        return archive.getEntry("pack.mcmeta")?.let {
            val stream = archive.getInputStream(it)
            Json.decodeFromStream<PackMeta>(stream)
        }
    }

    override fun getLang(lang: String): Map<String, String>? {
        return archive.entries().asSequence()
            .filterNot { it.isDirectory }
            .map { it.name.split('/') to it }
            .filter { (path, _) -> path.size == 3 }
            .filter { (path, _) -> path[1] == "lang" }
            .filter { (path, _) -> path.last().basename() == lang }
            .map { (path, entry) -> path.last() to archive.getInputStream(entry).reader() }
            .map { (name, reader) -> Language.parse(name, reader) }
            .map { it.translations }
            .reduceOrNull(Map<String, String>::plus)
    }
}
