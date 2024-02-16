package dev.mattsturgeon.assets

import dev.mattsturgeon.extensions.basename
import kotlinx.serialization.json.Json
import java.io.Reader
import java.util.function.Supplier

internal interface BaseAssets : Assets {

    fun getLangFiles(): Iterable<Pair<String, Supplier<Reader>>>

    fun getPackMetaFile(): Reader?

    override fun packMeta() = getPackMetaFile()?.run { Json.decodeFromString<PackMeta>(readText()) }

    override fun getLang(lang: String): Map<String, String>? {
        return getLangFiles()
            .filter { (name) -> lang == name.basename() }
            // And parse them using Language
            .map { (name, supplier) -> Language.parse(name, supplier.get()) }
            .map { it.translations }
            // Finally, combine all parsed files into one Map
            .reduceOrNull(Map<String, String>::plus)
    }

}