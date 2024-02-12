package dev.mattsturgeon.assets

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
internal data class Index(val objects: Map<String, AssetObject>) {
    fun get(path: String) = objects[path]

    @Serializable
    internal data class AssetObject(val hash: String, val size: ULong) {
        fun file(assetsDir: File): File = assetsDir
            .resolve("objects")
            .resolve(hash.substring(0, 2))
            .resolve(hash)
    }
}
