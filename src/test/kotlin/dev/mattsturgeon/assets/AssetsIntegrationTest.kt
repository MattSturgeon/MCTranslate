package dev.mattsturgeon.assets

import dev.mattsturgeon.minecraft.LanguageInfo
import dev.mattsturgeon.testing.makeZip
import dev.mattsturgeon.testing.resource
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AssetsIntegrationTest {

    private lateinit var simple: List<Assets>

    private val simpleDir = resource("integration/simpleAssets")!!
    private val simpleIndexed = resource("integration/simpleAssets.indexed")!!
    private val simpleZip = makeZip(simpleDir, "assets")

    private val legacyDir = resource("integration/legacyAssets")!!
    private val legacyIndexed = resource("integration/legacyAssets.indexed")!!
    private val legacyZip = makeZip(legacyDir, "assets")

    @BeforeTest
    fun setup() {
        simple = listOf(
            Assets.fromMinecraftAssets(simpleIndexed, "simple"),
            Assets.fromDirectory(simpleDir),
            Assets.fromZipFile(simpleZip),
            Assets.fromMinecraftAssets(legacyIndexed, "legacy"),
            Assets.fromDirectory(legacyDir),
            Assets.fromZipFile(legacyZip)
        )
    }

    @TestFactory
    fun `Finds language info in pack meta`(): List<DynamicTest> {
        val expectations = mapOf(
            "en_us" to LanguageInfo(
                name = "English",
                region = "US",
                bidirectional = false
            )
        )
        return simple.flatMap {
            expectations.map { (lang, expected) ->
                dynamicTest("""${it::class.simpleName}.getLangInfo("$lang") is correct""") {
                    assertEquals(expected, it.getLangInfo(lang))
                }
            }
        }
    }

    @TestFactory
    fun `Finds languages that exist`() = simple.map {
        dynamicTest("${it::class.simpleName}.getLang(\"en_us\") is not null") {
            assertNotNull(it.getTranslations("en_us"))
        }
    }

    @TestFactory
    fun `Missing languages fail gracefully`(): List<DynamicTest> {
        val codes = listOf("en_gb", "invalid", "asdofin428whq9wesad\$Rweq8\"23rq")

        return simple // list of every `assets to lang` combination
            .flatMap { codes.map { lang -> it to lang } }
            .map { (it, lang) ->
                dynamicTest("${it::class.simpleName}.getLang(\"${lang}\") fails gracefully") {
                    assertNull(it.getTranslations(lang))
                }
            }
    }

    @TestFactory
    fun `Finds correct translations`() = simple.map {
        dynamicTest("${it::class.simpleName}.getLang(\"en_us\") contains correct translations") {
            val translations = it.getTranslations("en_us")!!
            assertEquals("some value", translations["some.key"])
        }
    }
}