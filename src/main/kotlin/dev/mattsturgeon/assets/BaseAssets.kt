package dev.mattsturgeon.assets

import dev.mattsturgeon.dev.mattsturgeon.lang.Translations
import dev.mattsturgeon.dev.mattsturgeon.lang.decodeTranslations
import dev.mattsturgeon.dev.mattsturgeon.minecraft.PackMeta
import dev.mattsturgeon.extensions.basename
import kotlinx.serialization.json.Json
import java.io.Reader

internal typealias NamedSupplier = Pair<String, () -> Reader>
internal typealias NamedSuppliers = Map<String, () -> Reader>

internal interface BaseAssets : Assets {

    fun getLangFiles(): Iterable<NamedSupplier>

    fun getPackMetaFile(): Reader?

    override fun packMeta() = getPackMetaFile()?.run { Json.decodeFromString<PackMeta>(readText()) }

    override fun getTranslations(lang: String): Translations? {
        return getLangFiles()
            .filter { (name) -> lang == name.basename() }
            // And parse them using Language
            .map { (name, supplier) -> decodeTranslations(name, supplier()) }
            // Finally, combine all parsed files into one Map
            .reduceOrNull(Translations::plus)
    }

}