package dev.mattsturgeon.assets

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.util.function.Supplier

@OptIn(ExperimentalSerializationApi::class)
class MinecraftIndexedAssets(assetsDir: File, assetIndex: String) : IndexedAssets(run {
    val indexFile = assetsDir.resolve("indexes").resolve("$assetIndex.json")
    val index = Json.decodeFromStream<Index>(indexFile.inputStream())
    index.objects.entries.associate { (path, obj) -> Pair(path, Supplier { obj.file(assetsDir).reader() }) }
})

