package dev.mattsturgeon.assets

import kotlinx.serialization.Serializable

@Serializable
data class MCMeta(val languages: Map<String, LanguageInfo>) {
    operator fun plus(other: MCMeta) = MCMeta(this.languages + other.languages)
}
