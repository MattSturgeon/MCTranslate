package dev.mattsturgeon.assets

import kotlin.test.*

class IndexedAssetsTest {

    private lateinit var assetsWithPackMeta: Assets
    private lateinit var assetsWithoutPackMeta: Assets

    @BeforeTest
    fun setup() {
        assetsWithPackMeta = Assets.fromStrings(
            "pack.mcmeta" to """
                        {
                          "languages": {
                            "en_us": {
                              "name": "English",
                              "region": "US",
                              "bidirectional": false
                            }
                          }
                        }
                    """.trimIndent(),
            "minecraft/lang/en_us.json" to """
                        {
                          "some.key": "some value"
                        }
                    """.trimIndent()
        )

        assetsWithoutPackMeta = Assets.fromStrings(
            "minecraft/lang/en_us.json" to """
                        {
                          "some.key": "some value"
                        }
                    """.trimIndent()
        )
    }

    @Test
    fun `Finds pack mcmeta`() {
        assertNotNull(assetsWithPackMeta.packMeta(), "Has pack.mcmeta")
        assertNull(assetsWithoutPackMeta.packMeta(), "Doesn't have pack.mcmeta")
    }

    @Test
    fun `Pack mcmeta has correct entries`() {
        val languages = assetsWithPackMeta.packMeta()?.languages!!
        assertContains(languages, "en_us")
        assertEquals(languages["en_us"]?.name, "English")
        assertEquals(languages["en_us"]?.region, "US")
        assertEquals(languages["en_us"]?.bidirectional, false)
    }

    @Test
    fun `Doesn't find missing translations`() {
        sequenceOf(
            "en_gb",
            "t_it"
        ).forEach {
            assertNull(assetsWithPackMeta.getLang(it))
        }
    }

    @Test
    fun `Fails gracefully for invalid lang code`() {
        sequenceOf(
            "adsflshasdfouihasdf",
            "241liugwefs__43324kjn-asfkjn$.,easf''\"soaid6^5!£"
        ).forEach {
            assertNull(assetsWithPackMeta.getLang(it))
        }
    }

    @Test
    fun `Finds translations`() {
        val translations = assetsWithPackMeta.getLang("en_us")
        assertNotNull(translations)
    }

    @Test
    fun `Finds correct translations`() {
        val translations = assetsWithPackMeta.getLang("en_us")!!
        assertEquals("some value", translations["some.key"])
    }
}