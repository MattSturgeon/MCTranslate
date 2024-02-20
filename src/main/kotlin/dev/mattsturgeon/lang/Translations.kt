package dev.mattsturgeon.dev.mattsturgeon.lang

import kotlinx.serialization.json.Json
import java.io.Reader
import java.util.*

typealias Translation = Pair<String, String>

typealias Translations = Map<String, String>

fun decodeTranslations(name: String, reader: Reader): Translations {
    val extension = name.substringAfterLast('.', "")

    // Before 1.13 (18w02a), lang files used ".lang" extension and properties format
    return when (extension.lowercase()) {
        "json" -> decodeJsonFile(reader)
        "lang" -> decodePropertiesFile(reader)
        else -> throw IllegalArgumentException("""Cannot parse lang file with extension "$extension"!""")
    }
}

private fun decodeJsonFile(reader: Reader): Translations = Json.decodeFromString(reader.readText())

private fun decodePropertiesFile(reader: Reader): Translations {
    val props = Properties()
    props.load(reader)
    return props.entries.associate(Map.Entry<Any, Any>::toTranslation)
}

private fun Map.Entry<*, *>.toTranslation() = Translation(key.toString(), value.toString())
