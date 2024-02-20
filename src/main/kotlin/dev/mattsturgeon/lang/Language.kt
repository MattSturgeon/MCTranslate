package dev.mattsturgeon.dev.mattsturgeon.lang

import dev.mattsturgeon.dev.mattsturgeon.minecraft.Translations
import kotlinx.serialization.json.Json
import java.io.Reader
import java.util.*

data class Language(val code: String, val translations: Translations) {
    companion object {
        fun parse(name: String, reader: Reader): Language {
            val lang: String
            val extension: String

            name.lastIndexOf('.').let { index ->
                if (index < 0) {
                    throw IllegalArgumentException("Cannot parse lang file without extension!")
                }
                lang = name.substring(0, index)
                extension = name.substring(index + 1).lowercase()
            }

            val translations = when (extension) {
                "json" -> decodeJsonFile(reader)
                "lang" -> decodePropertiesFile(reader)
                else -> throw IllegalArgumentException("""Cannot parse lang file with extension "$extension"!""")
            }

            return Language(lang, translations)
        }

        private fun decodeJsonFile(reader: Reader): Translations = Json.decodeFromString(reader.readText())

        private fun decodePropertiesFile(reader: Reader): Translations {
            val props = Properties()
            props.load(reader)
            return props.entries.associate { (key, value) -> Pair(key.toString(), value.toString()) }
        }
    }
}
