import dev.mattsturgeon.assets.Assets
import dev.mattsturgeon.assets.DummyAssets
import dev.mattsturgeon.assets.StackedAssets
import kotlin.test.*

class StackedAssetsTest {

    private lateinit var a: Assets
    private lateinit var b: Assets

    @BeforeTest
    fun setup() {
        a = DummyAssets(mapOf(
            "foo" to "Bar",
            "bar" to "Other"
        ))
        b = DummyAssets(mapOf(
            "some" to "thing",
            "foo" to "duplicate path"
        ))
    }

    @Test
    fun `Combine into stacked assets`() {
        assertIs<StackedAssets>(a + b)
        assertIsNot<StackedAssets>(a)
        assertIsNot<StackedAssets>(b)
    }

    @Test
    fun `Flatten nested stacked assets`() {
        val stack = a + b
        sequenceOf(a + stack, b + stack, stack + b, stack + a)
            .map { it as StackedAssets }
            .map { it.children }
            .forEach { children ->
                assertEquals(3, children.size)
                children.forEach { assertIsNot<StackedAssets>(it) }
            }
    }
}