package dev.mattsturgeon.assets

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.util.Properties

data class Language(val code: String, val translations: Map<String, String>) {
    companion object {
        fun parse(file: File, name: String = file.name): Language {
            val lang: String
            val extension: String

            name.lastIndexOf('.').let { index ->
                if (index < 0) {
                    throw IllegalArgumentException("Cannot parse lang file without extension!")
                }
                lang = name.substring(0, index)
                extension = name.substring(index).lowercase()
            }

            val translations = when (extension) {
                "json" -> decodeJsonFile(file)
                "lang" -> decodePropertiesFile(file)
                else -> throw IllegalArgumentException("""Cannot parse lang file with extension "$extension"!""")
            }

            return Language(lang, translations)
        }

        @OptIn(ExperimentalSerializationApi::class)
        private fun decodeJsonFile(file: File): Map<String, String> = Json.decodeFromStream(file.inputStream())

        private fun decodePropertiesFile(file: File): Map<String, String> {
            val props = Properties()
            props.load(file.reader())
            return props.entries.associate { (key, value) -> Pair(key.toString(), value.toString()) }
        }
    }
}
