package dev.mattsturgeon.assets

import dev.mattsturgeon.dev.mattsturgeon.lang.Language
import dev.mattsturgeon.dev.mattsturgeon.lang.Translations
import dev.mattsturgeon.dev.mattsturgeon.minecraft.LanguageInfo
import dev.mattsturgeon.dev.mattsturgeon.minecraft.MinecraftAssetIndex
import dev.mattsturgeon.dev.mattsturgeon.minecraft.PackMeta
import dev.mattsturgeon.extensions.asset
import dev.mattsturgeon.extensions.isLower
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.Reader
import java.util.function.Supplier
import java.util.zip.ZipFile
import kotlin.streams.asSequence

interface Assets {

    fun packMeta(): PackMeta?

    fun getTranslations(lang: String): Translations?

    fun getLangInfo(lang: String): LanguageInfo? = packMeta()?.languages?.get(lang)

    fun getLanguage(lang: String): Language? {
        // Before 1.11 (16w32a), lang code was capitalized "en_US"
        val en = if (lang.isLower()) "en_us" else "en_US"

        return Language(
            code = lang,
            info = getLangInfo(lang),
            translations = getTranslations(lang) ?: return null,
            fallback = if (lang == en) null else getLanguage(en)
        )
    }

    operator fun plus(assets: Assets): Assets = when (assets) {
        is StackedAssets -> StackedAssets(this, *assets.children)
        else -> StackedAssets(this, assets)
    }

    companion object { // Factory methods

        /**
         * Build an [Assets] instance representing the minecraft [assetsDir].
         *
         * If an [assetIndex] is specified, then an [IndexedAssets] is constructed using the Minecraft asset index file.
         *
         * Otherwise, [FileAssets] is used.
         */
        @JvmStatic
        @JvmOverloads
        @OptIn(ExperimentalSerializationApi::class)
        fun fromMinecraftAssets(assetsDir: File, assetIndex: String? = null): Assets {
            if (assetIndex == null) {
                return FileAssets(assetsDir)
            }

            val index = assetsDir.resolve("indexes").resolve("$assetIndex.json")
            return IndexedAssets(
                Json.decodeFromStream<MinecraftAssetIndex>(index.inputStream())
                    .objects
                    .entries
                    .map { (path, obj) ->
                        path to Supplier { assetsDir.asset(obj).reader() }
                    })
        }

        @JvmStatic
        fun fromDirectory(file: File): Assets = FileAssets(file)

        @JvmStatic
        fun fromZipFile(file: File): Assets = fromZipFile(ZipFile(file))

        @JvmStatic
        fun fromZipFile(file: ZipFile): Assets =
            IndexedAssets(file.stream()
                .asSequence()
                .filterNot { it.isDirectory }
                .map { entry ->
                    entry.name to Supplier<Reader> { file.getInputStream(entry).reader() }
                }
                .asIterable())

        /**
         * Build an [Assets] instance containing the specified content.
         *
         * @param pairs pairs of `path` to `content`
         */
        @JvmStatic
        fun fromStrings(pairs: Iterable<Pair<String, String>>): Assets = IndexedAssets(
            pairs.map { (path, content) ->
                path to Supplier { content.reader() }
            })

        /**
         * Build an [Assets] instance containing the specified content.
         *
         * @param pairs pairs of `path` to `content`
         */
        @JvmStatic
        fun fromStrings(vararg pairs: Pair<String, String>) = fromStrings(pairs.asIterable())

        /**
         * Build an [Assets] instance containing the specified content.
         *
         * @param index map of `path` to `content`
         */
        @JvmStatic
        fun fromStrings(index: Map<String, String>) = fromStrings(index.map { it.toPair() })
    }

}
