package dev.mattsturgeon.dev.mattsturgeon.minecraft

import kotlinx.serialization.Serializable

/**
 * Represents Minecraft's assets _index_ file.
 *
 * Example snippet:
 * ```json
 * {
 *   "objects": {
 *     "minecraft/lang/af_za.json": {
 *       "hash": "d6ecd3c70e0259c6b4246807c4b49efb80bd8aac",
 *       "size": 436472
 *     },
 *     "minecraft/lang/ar_sa.json": {
 *       "hash": "96f1185dd550fd3756c80246114263cad64c8b78",
 *       "size": 518948
 *     },
 *     "minecraft/lang/ast_es.json": {
 *       "hash": "24cb6024c4bf04910cc2325f692f31692560c04f",
 *       "size": 444357
 *     }
 *   }
 * }
 * ```
 */
@Serializable
data class MinecraftAssetIndex(val objects: Map<String, AssetObject>)

@Serializable
data class AssetObject(val hash: String, val size: ULong)

@Serializable
data class PackMeta(val languages: Map<String, LanguageInfo>)

@Serializable
data class LanguageInfo(
    val name: String,
    val region: String,
    val bidirectional: Boolean
)

operator fun PackMeta.plus(other: PackMeta) = PackMeta(languages + other.languages)
