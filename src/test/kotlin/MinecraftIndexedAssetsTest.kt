import dev.mattsturgeon.assets.Assets
import dev.mattsturgeon.assets.MinecraftIndexedAssets
import kotlinx.serialization.SerializationException
import java.io.File
import kotlin.test.*

class MinecraftIndexedAssetsTest {

    private lateinit var simple1: Assets

    /**
     * @return a [File] pointing to the specified resource file, or `null`.
     */
    private fun resource(path: String): File? = this::class.java.getResource(path)?.run { File(toURI()) }

    @BeforeTest
    fun setup() {
        val assetsDir = resource("indexedAssets")!!
        simple1 = MinecraftIndexedAssets(assetsDir, "simple_1")
    }

    @Test
    fun `Doesn't find pack mcmeta`() {
        assertNull(simple1.packMeta(), "Doesn't have pack.mcmeta")
    }

    @Test
    fun `Doesn't find missing translations`() {
        sequenceOf("en_gb", "t_it").forEach {
            assertNull(simple1.getLang(it))
        }
    }

    @Test
    fun `Finds translations`() {
        sequenceOf("af_za", "ar_sa").forEach {
            assertNotNull(simple1.getLang(it))
        }
    }

    @Test
    fun `Finds correct translations`() {
        assertEquals("some value", simple1.getLang("af_za")!!["some.key"])
        assertEquals("some other value", simple1.getLang("ar_sa")!!["other.key"])
    }

    @Test
    fun `Fails to parse invalid lang file`() {
        assertFailsWith(SerializationException::class) {
            simple1.getLang("bad_lang")
        }
    }
}