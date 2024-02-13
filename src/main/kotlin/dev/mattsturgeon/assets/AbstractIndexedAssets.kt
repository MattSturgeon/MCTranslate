package dev.mattsturgeon.assets

import kotlinx.serialization.json.Json
import java.io.Reader
import java.util.function.Supplier

open class AbstractIndexedAssets(files: Map<String, Supplier<Reader>>) : Assets {

    // TODO store as a directory tree
    private val store: Map<List<String>, Supplier<Reader>> =
        files.mapKeys { (key, _) -> key.split('/') }

    override fun packMeta() =
        store[listOf("pack.mcmeta")]?.let { Json.decodeFromString<PackMeta>(it.get().readText()) }

    override fun getLang(lang: String): Map<String, String>? {
        return store.asSequence()
            .filter { (path, _) -> path.size == 3 }
            .filter { (path, _) -> path[1] == "lang" }
            .filter { (path, _) -> path.last().substringBeforeLast('.') == lang }
            .map { (path, supplier) -> Language.parse(path.last(), supplier.get()) }
            .map { it.translations }
            .reduceOrNull(Map<String, String>::plus)
    }
}