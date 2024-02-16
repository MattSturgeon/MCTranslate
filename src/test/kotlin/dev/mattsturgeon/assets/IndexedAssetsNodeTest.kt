package dev.mattsturgeon.assets

import dev.mattsturgeon.assets.IndexedAssets.*
import java.io.Reader
import java.util.function.Supplier
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexedAssetsNodeTest {

    private val expectations = mapOf(
        "file" to Expected("ROOT/some/path/down/to/file", "Some content"),
        "empty" to Expected("ROOT/some/path/down/empty")
    )

    private val simpleExpectation = mapOf(
        "a" to Expected("/a"),
        "b" to Expected("/b")
    )

    private lateinit var root: DirectoryNode
    private lateinit var simple: DirectoryNode

    @BeforeTest
    fun setup() {
        // Create a tree from expectations
        root = createTree(expectations, "ROOT")
        simple = createTree(simpleExpectation)
    }

    @Test
    fun `asPath() can handle simple nodes`() {
        simpleExpectation.entries.forEach { (key, expected) ->
            println("Checking \"$key\" has path: ${expected.path}")
            val node = expected.node!!
            assertEquals(expected.path, node.asPath(), "$key has correct path \"${expected.path}\"")
        }
    }

    @Test
    fun `asPath() can handle nested nodes`() {
        expectations.entries.forEach { (key, expected) ->
            println("Checking \"$key\" has path: ${expected.path}")
            val node = expected.node!!
            assertEquals(expected.path, node.asPath(), "$key has correct path \"${expected.path}\"")
        }
    }

    @Test
    fun `createTree works as expected`() {
        val pairs = expectations.values.map { (path, content, node) ->
            path.substringAfter('/') to Supplier<Reader> { content.reader() }
        }

        assertEquals(createTree(expectations), Node.createTree(pairs))
    }

    /**
     * Helper function to create a node tree from an expectations map.
     *
     * @param expectations the definition used to create the tree
     * @param rootName Optionally supply a name for the root node.
     * @return The root node of the tree.
     */
    private fun createTree(
        expectations: Map<String, Expected>,
        rootName: String = ""
    ): DirectoryNode {
        val root = DirectoryNode(rootName)

        expectations.values.forEach { expected ->
            expected.node = root.put(expected.path.split('/').drop(1)) { expected.content.reader() }
        }

        return root
    }

    private data class Expected(
        val path: String,
        val content: String = "",
        var node: Node? = null
    )
}