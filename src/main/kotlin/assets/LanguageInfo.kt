package dev.mattsturgeon.assets

import kotlinx.serialization.Serializable

@Serializable
data class LanguageInfo(
    val name: String,
    val region: String,
    val bidirectional: Boolean
)
