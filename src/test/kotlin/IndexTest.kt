import dev.mattsturgeon.assets.Index
import dev.mattsturgeon.assets.IndexedAssets
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language
import java.io.File
import kotlin.test.Test
import kotlin.test.asserter

class IndexTest {
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

        val index = Json.decodeFromString<Index>(json)

        asserter.assertEquals("Has 3 objects", 3, index.objects.size)

        val afZa = index.objects["minecraft/lang/af_za.json"]
        asserter.assertNotNull("Has af_za", afZa)
        asserter.assertEquals("Has af_za hash", "d6ecd3c70e0259c6b4246807c4b49efb80bd8aac", afZa?.hash)
    }

    @Test
    fun `Some test`() {
        IndexedAssets(File("foo"), "some index")
    }
}