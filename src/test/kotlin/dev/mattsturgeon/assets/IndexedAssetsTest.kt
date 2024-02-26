package dev.mattsturgeon.assets

import kotlin.test.*

class IndexedAssetsTest {

    private lateinit var assets: Assets

    @BeforeTest
    fun setup() {
        assets = Assets.fromStrings(
            "minecraft/lang/en_us.json" to """
                        {
                          "some.key": "some value"
                        }
                    """.trimIndent()
        )
    }

    @Test
    fun `Doesn't find missing translations`() {
        sequenceOf(
            "en_gb",
            "t_it"
        ).forEach {
            assertNull(assets.getTranslations(it))
        }
    }

    @Test
    fun `Fails gracefully for invalid lang code`() {
        sequenceOf(
            "adsflshasdfouihasdf",
            "241liugwefs__43324kjn-asfkjn$.,easf''\"soaid6^5!£"
        ).forEach {
            assertNull(assets.getTranslations(it))
        }
    }

    @Test
    fun `Finds translations`() {
        val translations = assets.getTranslations("en_us")
        assertNotNull(translations)
    }

    @Test
    fun `Finds correct translations`() {
        val translations = assets.getTranslations("en_us")!!
        assertEquals("some value", translations["some.key"])
    }
}