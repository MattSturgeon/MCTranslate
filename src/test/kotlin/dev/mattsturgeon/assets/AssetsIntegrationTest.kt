package dev.mattsturgeon.assets

import dev.mattsturgeon.testing.resource
import kotlin.test.*

class AssetsIntegrationTest {

    private var simple = emptyList<Assets>()

    @BeforeTest
    fun setup() {
        simple = listOf(
            Assets.fromDirectory(resource("integration/simpleAssets")!!),
            Assets.fromZipFile(resource("integration/simpleAssets.zip")!!)
        )
    }

    @Test
    fun `Can find pack mcmeta`() {
        simple.forEach {
            assertNotNull(it.packMeta())
        }
    }

    @Test
    fun `Pack mcmeta has correct entries`() {
        simple.forEach {
            val languages = it.packMeta()?.languages!!
            assertContains(languages, "en_us")
            assertEquals(languages["en_us"]?.name, "English")
            assertEquals(languages["en_us"]?.region, "US")
            assertEquals(languages["en_us"]?.bidirectional, false)
        }
    }

    @Test
    fun `Finds languages that exist`() {
        simple.forEach {
            assertNotNull(it.getLang("en_us"))
        }
    }

    @Test
    fun `Missing languages fail gracefully`() {
        simple.forEach {
            sequenceOf("en_gb", "invalid", "asdofin428whq9wesad\$Rweq8\"23rq").forEach { lang ->
                assertNull(it.getLang(lang))
            }
        }
    }

    @Test
    fun `Finds correct translations`() {
        simple.forEach {
            val translations = it.getLang("en_us")!!
            assertEquals("some value", translations["some.key"])
        }
    }
}