package dev.mattsturgeon.assets

import dev.mattsturgeon.dev.mattsturgeon.minecraft.MinecraftAssetIndex
import dev.mattsturgeon.extensions.asset
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.asserter

class MinecraftAssetIndexTest {
    @Test
    fun `Serialize index`() {
        @Language("JSON") val json = """
            {
              "objects": {
                "minecraft/lang/af_za.json": {
                  "hash": "d6ecd3c70e0259c6b4246807c4b49efb80bd8aac",
                  "size": 436472
                },
                "minecraft/lang/ar_sa.json": {
                  "hash": "96f1185dd550fd3756c80246114263cad64c8b78",
                  "size": 518948
                },
                "minecraft/lang/ast_es.json": {
                  "hash": "24cb6024c4bf04910cc2325f692f31692560c04f",
                  "size": 444357
                }
              }
            }
        """.trimIndent()

        val index: MinecraftAssetIndex = Json.decodeFromString(json)

        asserter.assertEquals("Has 3 objects", 3, index.objects.size)

        val afZa = index.objects["minecraft/lang/af_za.json"]
        asserter.assertNotNull("Has af_za", afZa)
        asserter.assertEquals("Has af_za hash", "d6ecd3c70e0259c6b4246807c4b49efb80bd8aac", afZa?.hash)
    }

    @Test
    fun `Correctly resolve object`() {
        val base = File("foo")
        mapOf(
            "d6ecd3c70e0259c6b4246807c4b49efb80bd8aac" to "objects/d6/d6ecd3c70e0259c6b4246807c4b49efb80bd8aac",
            "96f1185dd550fd3756c80246114263cad64c8b78" to "objects/96/96f1185dd550fd3756c80246114263cad64c8b78",
            "24cb6024c4bf04910cc2325f692f31692560c04f" to "objects/24/24cb6024c4bf04910cc2325f692f31692560c04f",
            "invalid" to "objects/in/invalid",
            "0_[]asdfpoijadsg34qt2redvp9sh£%QT£GVs" to "objects/0_/0_[]asdfpoijadsg34qt2redvp9sh£%QT£GVs"
        ).forEach { (hash, expected) ->
            assertEquals(base.resolve(expected), base.asset(hash))
        }
    }
}