package dev.mattsturgeon.assets

import dev.mattsturgeon.dev.mattsturgeon.minecraft.LanguageInfo
import dev.mattsturgeon.testing.resource
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.*

class AssetsIntegrationTest {

    private var simple = emptyList<Assets>()

    @BeforeTest
    fun setup() {
        simple = listOf(
            Assets.fromMinecraftAssets(resource("integration/simpleAssets.indexed")!!, "simple"),
            Assets.fromDirectory(resource("integration/simpleAssets")!!),
            Assets.fromZipFile(resource("integration/simpleAssets.zip")!!)
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