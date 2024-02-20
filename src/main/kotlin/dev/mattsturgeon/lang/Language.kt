package dev.mattsturgeon.dev.mattsturgeon.lang

import dev.mattsturgeon.assets.Assets
import dev.mattsturgeon.dev.mattsturgeon.minecraft.LanguageInfo

data class Language(
    val code: String,
    val info: LanguageInfo? = null,
    private val translations: Translations,
    private val fallback: Language? = null
) {
    fun getOr(key: String, default: String) = get(key) ?: default
    fun getOr(key: String, supplier: () -> String) = get(key) ?: supplier()
    operator fun get(key: String): String? = translations[key] ?: fallback?.get(key)
}
