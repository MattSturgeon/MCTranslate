package dev.mattsturgeon.assets

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

    fun getLang(lang: String): Map<String, String>?

    fun getLangInfo(lang: String): LanguageInfo? = packMeta()?.languages?.get(lang)

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
         * Otherwise, [DirAssets] is used.
         */
        @JvmOverloads
        @OptIn(ExperimentalSerializationApi::class)
        fun fromMinecraftAssets(assetsDir: File, assetIndex: String? = null): Assets {
            if (assetIndex == null) {
                return DirAssets(assetsDir)
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

        fun fromDirectory(file: File): Assets = DirAssets(file)

        fun fromZipFile(file: File): Assets = fromZipFile(ZipFile(file))
        fun fromZipFile(file: ZipFile): Assets =
            IndexedAssets(file.stream()
                .asSequence()
                .map { entry ->
                    entry.name to Supplier<Reader> { file.getInputStream(entry).reader() }
                }
                .asIterable())

        /**
         * Build an [Assets] instance containing the specified content.
         *
         * @param pairs pairs of `path` to `content`
         */
        fun fromStrings(pairs: Iterable<Pair<String, String>>): Assets = IndexedAssets(
            pairs.map { (path, content) ->
                path to Supplier { content.reader() }
            })

        /**
         * Build an [Assets] instance containing the specified content.
         *
         * @param pairs pairs of `path` to `content`
         */
        fun fromStrings(vararg pairs: Pair<String, String>) = fromStrings(pairs.asIterable())

        /**
         * Build an [Assets] instance containing the specified content.
         *
         * @param index map of `path` to `content`
         */
        fun fromStrings(index: Map<String, String>) = fromStrings(index.map { it.toPair() })
    }

}
