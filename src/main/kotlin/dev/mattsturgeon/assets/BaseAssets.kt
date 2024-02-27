package dev.mattsturgeon.assets

import dev.mattsturgeon.extensions.basename
import dev.mattsturgeon.lang.Translations
import dev.mattsturgeon.lang.decodeTranslations
import dev.mattsturgeon.minecraft.LanguageInfo
import dev.mattsturgeon.minecraft.decodePackMeta
import java.io.Reader

internal typealias NamedSupplier = Pair<String, () -> Reader>
internal typealias NamedSuppliers = Map<String, () -> Reader>

internal sealed class BaseAssets : Assets {

    private val infos: Map<String, LanguageInfo> by lazy {
        getPackMetaFile()?.let { decodePackMeta(it) } ?: emptyMap()
    }

    protected abstract fun getLangFiles(): Iterable<NamedSupplier>

    protected abstract fun getPackMetaFile(): Reader?

    override fun getLangInfo(lang: String): LanguageInfo? = infos[lang.lowercase()]

    override fun getTranslations(lang: String): Translations? {
        // TODO cache lazily
        return getLangFiles()
            // Case-insensitive to support legacy versions
            // Before 1.11 (16w32a), lang code was capitalized "en_US"
            .filter { (name) -> name.basename().equals(lang, ignoreCase = true) }
            // And parse them using Language
            .map { (name, supplier) -> decodeTranslations(name, supplier()) }
            // Finally, combine all parsed files into one Map
            .reduceOrNull(Translations::plus)
    }

}