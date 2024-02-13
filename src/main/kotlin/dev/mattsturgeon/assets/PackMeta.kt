package dev.mattsturgeon.assets

import kotlinx.serialization.Serializable

@Serializable
data class PackMeta(val languages: Map<String, LanguageInfo>) {
    operator fun plus(other: PackMeta) = PackMeta(languages + other.languages)
}
