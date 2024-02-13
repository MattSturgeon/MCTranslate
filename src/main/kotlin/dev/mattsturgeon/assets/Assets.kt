package dev.mattsturgeon.assets

interface Assets {

    fun packMeta(): PackMeta?

    fun getLang(lang: String): Map<String, String>?

    fun getLangInfo(lang: String): LanguageInfo? = packMeta()?.languages?.get(lang)

    operator fun plus(assets: Assets): Assets = when (assets) {
        is StackedAssets -> StackedAssets(this, *assets.children)
        else -> StackedAssets(this, assets)
    }

}
