package dev.mattsturgeon.assets

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
class IndexedAssets(private val assetsDir: File, assetIndex: String) : Assets {

    private val index: Index

    init {
        val indexFile = assetsDir.resolve("indexes").resolve("$assetIndex.json")
        index = Json.decodeFromStream(indexFile.inputStream())
    }

    private fun mcmetaFile() = index.objects["pack.mcmeta"]?.file(assetsDir)
    override fun mcmeta() = mcmetaFile()?.let { Json.decodeFromStream<MCMeta>(it.inputStream()) }

    override fun getLang(lang: String): Map<String, String>? {
        return index.objects
            .asSequence()
            .filter { (path, _) ->
                val parts = path.split("/")
                parts.size == 3 && parts[1] == "lang" && parts[2].substringBeforeLast('.') == lang
            }
            .map { (path, obj) ->
                Language.parse(file = obj.file(assetsDir), name = path.substringAfterLast('/'))
            }
            .map { it.translations }
            .reduceOrNull(Map<String, String>::plus)
    }
}