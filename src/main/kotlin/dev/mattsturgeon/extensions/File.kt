package dev.mattsturgeon.extensions

import dev.mattsturgeon.minecraft.AssetObject
import java.io.File

fun File.childFiles() = listFiles { file -> file.isFile } ?: emptyArray()
fun File.childDirectories() = listFiles { file -> file.isDirectory } ?: emptyArray()

/**
 * Resolve the [asset object][AssetObject] treating this file as the root `assetsDir`.
 */
fun File.asset(obj: AssetObject) = this.asset(obj.hash)

/**
 * Resolve the asset object with the given `hash`, treating this file as the root `assetsDir`.
 */
fun File.asset(hash: String) = this
    .resolve("objects")
    .resolve(hash.substring(0, 2))
    .resolve(hash)
