import dev.mattsturgeon.assets.IndexedAssets.*
import kotlin.reflect.KClass
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexedAssetsNodeTest {

    private val expectations = mapOf(
        "root" to Expected("ROOT", null, DirectoryNode::class),
        "file" to Expected("ROOT/some/path/down/to/file", "Some content", FileNode::class),
        "directory" to Expected("ROOT/some/path/down/to", null, DirectoryNode::class)
    )

    private val simpleExpectation = mapOf(
        "root" to Expected("", null, DirectoryNode::class),
        "a" to Expected("/a", null, DirectoryNode::class),
        "b" to Expected("/b", null, DirectoryNode::class)
    )

    private lateinit var root: DirectoryNode
    private lateinit var simple: DirectoryNode

    @BeforeTest
    fun setup() {
        // Create a tree from expectations
        root = createTree(expectations)
        simple = createTree(simpleExpectation)
    }

    @Test
    fun `asPath() can handle simple nodes`() {
        simpleExpectation.entries.forEach { (key, expected) ->
            println("Checking \"$key\" has path: ${expected.path} (${expected.type.simpleName})")
            val node = expected.node!!
            assertEquals(expected.path, node.asPath(), "$key has correct path \"${expected.path}\"")
        }
    }

    @Test
    fun `asPath() can handle nested nodes`() {
        expectations.entries.forEach { (key, expected) ->
            println("Checking \"$key\" has path: ${expected.path} (${expected.type.simpleName})")
            val node = expected.node!!
            assertEquals(expected.path, node.asPath(), "$key has correct path \"${expected.path}\"")
        }
    }

    /**
     * Helper function to create a node tree from an expectations map.
     *
     * @param expectations the definition used to create the tree
     * @param root Optionally supply a root node. Will be mutated.
     * @return The root node of the tree. Points to the same object as [root].
     */
    private fun createTree(
        expectations: Map<String, Expected<*>>,
        root: DirectoryNode = run { DirectoryNode(expectations["root"]!!.path) }
    ): DirectoryNode {
        // Do this manually to prevent the test being modified externally
        expectations.values.forEach { expected ->
            // Creates a FileNode if content isn't null,
            // otherwise creates a DirectoryNode

            // Create branch node(s)
            val dir = expected.path
                .split('/') // Split path into steps
                .drop(1) // Without "ROOT"
                .dropLast(expected.content?.let { 1 } ?: 0) // Without filename
                .fold(root) { dir, name -> dir.makeDirectory(name) }

            // Create(?) and assign leaf node
            expected.node = expected.content?.let { content ->
                val name = expected.path.substringAfterLast('/')
                dir.makeFile(name) { content.reader() }
            } ?: dir
        }

        return root
    }

    private data class Expected<T : Node>(
        val path: String,
        val content: String?,
        val type: KClass<T>,
        var node: Node? = null
    )
}