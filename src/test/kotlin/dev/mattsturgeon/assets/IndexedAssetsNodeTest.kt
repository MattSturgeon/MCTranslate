package dev.mattsturgeon.assets

import dev.mattsturgeon.assets.IndexedAssets.DirectoryNode
import dev.mattsturgeon.assets.IndexedAssets.Node
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.Reader.nullReader
import kotlin.random.Random
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

    @TestFactory
    fun `put supports trailing slashes`(): List<DynamicTest> {
        val expectations = mapOf(
            "/some/file" to "/some/file/",
            "/foo/bar/baz" to "//foo//bar//baz//",
            "/extreme" to "/extreme/////////////",
        )

        return expectations.map { (expect, input) ->
            dynamicTest("Handles \"$input\"") {
                val node = DirectoryNode("")
                node.put(input) { nullReader() }
                assertEquals(listOf(expect), node.allPaths())
            }
        }
    }

    @Test
    fun `allPaths() matches input`() {
        // Sorted dirs-first, then alphabetical
        val paths = listOf(
            "/a/b/z/x",
            "/a/b/c",
            "/a/b/f",
            "/foo/bar/baz",
            "/foo/sub/thing",
            "/some/zoo/animal",
            "/some/other",
            "/some/xray",
            "/aaa",
            "/mmm",
            "/path",
            "/zzz",
        )

        val random = Random(10)
        val unsorted = paths.sortedBy { random.nextInt(-1, 1) }
        val node = createTree(unsorted)

        assertEquals(paths, node.allPaths())
    }

    @Test
    fun `createTree() works as expected`() {
        val pairs = expectations.values.map { (path, content, node) ->
            path.substringAfter('/') to { content.reader() }
        }

        val a = createTree(expectations)
        val b = Node.createTree(pairs)

        assertEquals(a.allPaths(), b.allPaths())
    }

    private fun createTree(paths: Iterable<String>) = Node.createTree(paths.map { it to { nullReader() } })

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