package dev.mattsturgeon.assets

import dev.mattsturgeon.dev.mattsturgeon.lang.Translations
import dev.mattsturgeon.dev.mattsturgeon.minecraft.LanguageInfo

internal class StackedAssets(internal vararg val children: Assets) : Assets {

    override fun getTranslations(lang: String): Translations? = children
        .mapNotNull { it.getTranslations(lang) }
        .reduceOrNull(Translations::plus)

    override fun getLangInfo(lang: String): LanguageInfo? = children.fold(null) { info: LanguageInfo?, it ->
        // TODO to support merging, LanguageInfo's properties would need to be nullable
        it.getLangInfo(lang) ?: info
    }

    override fun plus(assets: Assets): Assets = when (assets) {
        is StackedAssets -> StackedAssets(*children, *assets.children)
        else -> StackedAssets(*children, assets)
    }
}