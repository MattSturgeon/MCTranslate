package dev.mattsturgeon.assets

import dev.mattsturgeon.extensions.basename
import kotlinx.serialization.json.Json
import java.io.Reader
import java.util.function.Supplier

class IndexedAssets(pairs: Iterable<Pair<String, Supplier<Reader>>>) : Assets {

    constructor(vararg pairs: Pair<String, Supplier<Reader>>) : this(pairs.asIterable())
    constructor(index: Map<String, Supplier<Reader>>) : this(index.map { it.toPair() })

    /**
     * A virtual directory tree
     */
    private val root = Node.createTree(pairs)

    override fun packMeta() = root.getFile("pack.mcmeta")?.let {
        val text = it.supplier.get().readText()
        Json.decodeFromString<PackMeta>(text)
    }

    override fun getLang(lang: String): Map<String, String>? {
        return root.directories.asSequence()
            // Search all top-level dirs for "lang" sub-dirs
            .mapNotNull { it.getDirectory("lang") }
            // Then get all files matching the requested lang
            .flatMap { it.files }
            .filter { lang == it.name.basename() }
            // And parse them using Language
            .map { Language.parse(it.name, it.supplier.get()) }
            .map { it.translations }
            // Finally, combine all parsed files into one Map
            .reduceOrNull(Map<String, String>::plus)
    }

    internal interface Node {
        val name: String
        val parent: Node?

        fun asPath(): String = parent?.let { "${it.asPath()}/$name" } ?: name

        companion object {
            fun createTree(pairs: Iterable<Pair<String, Supplier<Reader>>>): DirectoryNode {
                val root = DirectoryNode("")
                pairs.forEach { (path, supplier) ->
                    root.put(path, supplier)
                }
                return root
            }
        }
    }

    internal data class FileNode(
        override val name: String,
        override val parent: DirectoryNode,
        val supplier: Supplier<Reader>
    ) : Node

    internal data class DirectoryNode(
        override val name: String,
        override val parent: DirectoryNode? = null,
        val directories: MutableList<DirectoryNode> = mutableListOf(),
        val files: MutableList<FileNode> = mutableListOf()
    ) : Node {
        fun getFile(path: String) = getFile(path.split('/'))

        fun getFile(path: List<String>): FileNode? {
            // Ignore empty path segments
            val steps = path.dropWhile(String::isEmpty)
            steps.ifEmpty { return null }

            // End of path: get a file
            steps.singleOrNull()?.let { name ->
                return files.firstOrNull { it.name == name }
            }

            // Recursively follow path into subdirectory
            return directories.firstOrNull { it.name == steps.first() }?.getFile(steps.drop(1))
        }

        fun getDirectory(path: String) = getDirectory(path.split('/'))

        fun getDirectory(path: List<String>): DirectoryNode? {
            // Ignore empty path segments
            val steps = path.dropWhile(String::isEmpty)

            // No path left: we've found the directory
            steps.ifEmpty { return this }

            // Recursively follow path
            return directories.firstOrNull { it.name == steps.first() }?.getDirectory(steps.drop(1))
        }

        fun allPaths(): List<String> {
            val d = directories.flatMap { it.allPaths() }
            val f = files.map { it.asPath() }
            return d + f
        }

        fun put(path: String, supplier: Supplier<Reader>) = put(path.split('/'), supplier)

        fun put(path: List<String>, supplier: Supplier<Reader>): FileNode {
            // Ignore empty path segments
            val steps = path.dropWhile(String::isEmpty)

            steps.ifEmpty {
                throw IllegalArgumentException("""Path is empty (or contains only empty segments): "${path.joinToString("/")}".""")
            }

            // Base case: reached the end of the path
            steps.singleOrNull()?.let {
                return makeFileNode(it, supplier)
            }

            // Otherwise, recurse into a directory node
            return makeDirectoryNode(steps.first()).put(steps.drop(1), supplier)
        }

        private fun makeFileNode(name: String, supplier: Supplier<Reader>): FileNode {
            files.firstOrNull { it.name == name }?.let {
                throw IllegalArgumentException("""A file named "${it.asPath()}" already exists.""")
            }

            directories.firstOrNull { it.name == name }?.let {
                throw IllegalArgumentException("""A directory named "${it.asPath()}" already exists.""")
            }

            val node = FileNode(parent = this, name = name, supplier = supplier)
            files.add(node)
            files.sortBy { it.name }
            return node
        }

        private fun makeDirectoryNode(name: String): DirectoryNode {
            files.firstOrNull { it.name == name }?.let {
                throw IllegalArgumentException("""A file named "${it.asPath()}" already exists.""")
            }

            // Return the existing node if one exists
            directories.firstOrNull() { it.name == name }?.let {
                return it
            }

            // Otherwise create a new one
            val node = DirectoryNode(parent = this, name = name)
            directories.add(node)
            directories.sortBy { it.name }
            return node
        }

    }
}