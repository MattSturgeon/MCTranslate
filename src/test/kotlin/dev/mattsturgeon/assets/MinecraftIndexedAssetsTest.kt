package dev.mattsturgeon.assets

import dev.mattsturgeon.testing.resource
import kotlinx.serialization.SerializationException
import kotlin.test.*

class MinecraftIndexedAssetsTest {

    private lateinit var simple1: Assets

    @BeforeTest
    fun setup() {
        val assetsDir = resource("indexedAssets")!!
        simple1 = Assets.fromMinecraftAssets(assetsDir, "simple_1")
    }

    @Test
    fun `Doesn't find missing translations`() {
        sequenceOf("en_gb", "t_it").forEach {
            assertNull(simple1.getTranslations(it))
        }
    }

    @Test
    fun `Finds translations`() {
        sequenceOf("af_za", "ar_sa").forEach {
            assertNotNull(simple1.getTranslations(it))
        }
    }

    @Test
    fun `Finds correct translations`() {
        assertEquals("some value", simple1.getTranslations("af_za")!!["some.key"])
        assertEquals("some other value", simple1.getTranslations("ar_sa")!!["other.key"])
    }

    @Test
    fun `Fails to parse invalid lang file`() {
        assertFailsWith(SerializationException::class) {
            simple1.getTranslations("bad_lang")
        }
    }
}