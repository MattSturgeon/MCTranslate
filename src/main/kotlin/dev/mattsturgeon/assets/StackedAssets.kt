package dev.mattsturgeon.assets

import dev.mattsturgeon.dev.mattsturgeon.lang.Translations
import dev.mattsturgeon.dev.mattsturgeon.minecraft.LanguageInfo

internal class StackedAssets(internal vararg val children: Assets) : Assets {

    override fun getTranslations(lang: String): Translations? = children.asSequence()
        .mapNotNull { it.getTranslations(lang) }
        .reduceOrNull(Translations::plus)

    override fun getLangInfo(lang: String): LanguageInfo? = children.asSequence()
        .mapNotNull { it.getLangInfo(lang) }
        .reduceOrNull(LanguageInfo::plus)

    override fun plus(assets: Assets): Assets = when (assets) {
        is StackedAssets -> StackedAssets(*children, *assets.children)
        else -> StackedAssets(*children, assets)
    }
}