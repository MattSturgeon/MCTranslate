package dev.mattsturgeon.assets

import java.io.Reader
import java.util.*
import java.util.function.Supplier

internal class IndexedAssets(pairs: Iterable<Pair<String, Supplier<Reader>>>) : BaseAssets {

    constructor(vararg pairs: Pair<String, Supplier<Reader>>) : this(pairs.asIterable())
    constructor(index: Map<String, Supplier<Reader>>) : this(index.map { it.toPair() })

    /**
     * A virtual directory tree
     */
    private val root = Node.createTree(pairs)

    override fun getPackMetaFile(): Reader? {
        return root.getFile("pack.mcmeta")?.supplier?.get()
    }

    override fun getLangFiles(): Iterable<Pair<String, Supplier<Reader>>> {
        // Search all top-level dirs for "lang" sub-dirs
        // then return all files in the "lang" dirs
        return root.directories.asSequence()
            .mapNotNull { it.getDirectory("lang") }
            .flatMap { it.files }
            .map { it.name to it.supplier }
            .asIterable()
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
    ) : Node {

        // Sorted by name
        val directories: MutableSet<DirectoryNode> = TreeSet { a, b -> a.name.compareTo(b.name) }
        val files: MutableSet<FileNode> = TreeSet { a, b -> a.name.compareTo(b.name) }

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
            return node
        }

    }
}