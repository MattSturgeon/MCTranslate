package dev.mattsturgeon.assets

import kotlinx.serialization.json.Json

class DummyAssets(private val files: Map<String, String>) : Assets {
    override fun packMeta() = files["pack.mcmeta"]?.let { Json.decodeFromString<PackMeta>(it) }

    override fun getLang(lang: String): Map<String, String>? {
        return files.asSequence()
            .filter { (path, _) ->
                val parts = path.split("/")
                parts.size == 3 && parts[1] == "lang" && parts[2].substringBeforeLast('.') == lang
            }
            .map { (path, obj) ->
                Language.parse(reader = obj.reader(), name = path.substringAfterLast('/'))
            }
            .map { it.translations }
            .reduceOrNull(Map<String, String>::plus)
    }
}