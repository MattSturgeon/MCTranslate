package dev.mattsturgeon.assets

import dev.mattsturgeon.extensions.asset
import dev.mattsturgeon.extensions.startsWith
import dev.mattsturgeon.lang.Language
import dev.mattsturgeon.lang.Translations
import dev.mattsturgeon.minecraft.LanguageInfo
import dev.mattsturgeon.minecraft.MinecraftAssetIndex
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.util.zip.ZipFile
import kotlin.streams.asSequence

interface Assets {

    fun getTranslations(lang: String): Translations?

    fun getLangInfo(lang: String): LanguageInfo?

    fun getLanguage(lang: String): Language? = getLanguage(lang, "en_us")

    fun getLanguage(lang: String, fallback: String): Language? {
        // Return early if lang is missing
        // TODO consider if we should return fallback?
        val translations = getTranslations(lang)
            ?: return null

        // Get fallback if it's different
        // Case-insensitive to support legacy versions
        val fb =
            if (lang.equals(fallback, ignoreCase = true)) null
            else getLanguage(fallback)

        val info = getLangInfo(lang)

        return Language(lang, info, translations, fb)
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
                        path to { assetsDir.asset(obj).reader() }
                    })
        }

        @JvmStatic
        fun fromDirectory(file: File): Assets = FileAssets(file)

        @JvmStatic
        @JvmOverloads
        fun fromZipFile(file: File, path: String = "assets"): Assets = fromZipFile(ZipFile(file), path)

        /**
         * Build an [Assets] instance using the content of the [ZipFile] provided.
         *
         * If [path] is non-empty, it specifies the path _within_ the zip file where the assets are located.
         *
         * [path] defaults to `"assets"`.
         */
        @JvmStatic
        @JvmOverloads
        fun fromZipFile(file: ZipFile, path: String = "assets"): Assets {
            // We will filter for entries "in" this path
            // We'll also drop the prefix from the indexed path
            val prefix = path.split('/').filterNot(String::isEmpty)

            return IndexedAssets(file.stream()
                .asSequence()
                .filterNot { it.isDirectory }
                .map {
                    // Split up entry path
                    it.name.split('/').filterNot(String::isEmpty) to it
                }
                .filter { (path, _) ->
                    // Filter out entries not in the specified path
                    path.startsWith(prefix)
                }
                .map { (path, entry) ->
                    // Drop the prefix from the start of the path
                    path.drop(prefix.size).joinToString("/") to { file.getInputStream(entry).reader() }
                }
                .asIterable())
        }

        /**
         * Build an [Assets] instance containing the specified content.
         *
         * @param pairs pairs of `path` to `content`
         */
        @JvmStatic
        fun fromStrings(pairs: Iterable<Pair<String, String>>): Assets = IndexedAssets(
            pairs.map { (path, content) ->
                path to { content.reader() }
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
