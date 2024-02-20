package dev.mattsturgeon.assets

import dev.mattsturgeon.dev.mattsturgeon.lang.Translations
import dev.mattsturgeon.dev.mattsturgeon.minecraft.PackMeta
import dev.mattsturgeon.dev.mattsturgeon.minecraft.plus

internal class StackedAssets(internal vararg val children: Assets) : Assets {

    override fun packMeta() = children
        .mapNotNull(Assets::packMeta)
        .reduceOrNull(PackMeta::plus)

    override fun getTranslations(lang: String): Translations? = children
        .mapNotNull { it.getTranslations(lang) }
        .reduceOrNull(Translations::plus)

    override fun plus(assets: Assets): Assets = when (assets) {
        is StackedAssets -> StackedAssets(*children, *assets.children)
        else -> StackedAssets(*children, assets)
    }
}