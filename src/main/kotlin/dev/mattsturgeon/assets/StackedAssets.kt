package dev.mattsturgeon.assets

internal class StackedAssets(internal vararg val children: Assets) : Assets {

    override fun packMeta() = children
        .mapNotNull(Assets::packMeta)
        .reduceOrNull(PackMeta::plus)

    override fun getLang(lang: String) = children
        .mapNotNull { it.getLang(lang) }
        .reduceOrNull(Map<String, String>::plus)

    override fun plus(assets: Assets): Assets = when (assets) {
        is StackedAssets -> StackedAssets(*children, *assets.children)
        else -> StackedAssets(*children, assets)
    }
}