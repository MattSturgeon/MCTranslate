package dev.mattsturgeon.assets

interface Assets {

    fun mcmeta(): MCMeta?

    fun getLang(lang: String): Map<String, String>?

    fun getLangInfo(lang: String): LanguageInfo? = mcmeta()?.languages?.get(lang)

    operator fun plus(assets: Assets): Assets = when (assets) {
        is StackedAssets -> StackedAssets(this, *assets.children)
        else -> StackedAssets(this, assets)
    }

}
