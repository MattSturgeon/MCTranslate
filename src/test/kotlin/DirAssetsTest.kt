import dev.mattsturgeon.assets.Assets
import dev.mattsturgeon.assets.DirAssets
import kotlin.test.*

class DirAssetsTest {

    private lateinit var simple: Assets

    @BeforeTest
    fun setup() {
        simple = DirAssets(resource("simpleAssets")!!)
    }

    @Test
    fun `Can find pack mcmeta`() {
        assertNotNull(simple.packMeta())
    }

    @Test
    fun `Pack mcmeta has correct entries`() {
        val languages = simple.packMeta()?.languages!!
        assertContains(languages, "en_us")
        assertEquals(languages["en_us"]?.name, "English")
        assertEquals(languages["en_us"]?.region, "US")
        assertEquals(languages["en_us"]?.bidirectional, false)
    }

    @Test
    fun `Finds languages that exist`() {
        assertNotNull(simple.getLang("en_us"))
    }

    @Test
    fun `Missing languages fail gracefully`() {
        sequenceOf("en_gb", "invalid", "asdofin428whq9wesad\$Rweq8\"23rq").forEach {
            assertNull(simple.getLang(it))
        }
    }

    @Test
    fun `Finds correct translations`() {
        val translations = simple.getLang("en_us")!!
        assertEquals("some value", translations["some.key"])
    }
}