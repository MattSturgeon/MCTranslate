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
        return root.listDirectories().asSequence()
            .mapNotNull { it.getDirectory("lang") }
            .flatMap { it.listFiles().asSequence() }
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

        // Indexed & sorted by name
        private val directories: MutableMap<String, DirectoryNode> = TreeMap()
        private val files: MutableMap<String, FileNode> = TreeMap()

        fun getFile(path: String) = getFile(path.split('/'))

        fun getFile(path: List<String>): FileNode? {
            // Ignore empty path segments
            val steps = path.dropWhile(String::isEmpty)

            // End of path
            steps.ifEmpty { return null }
            steps.singleOrNull()?.let { return files[it] }

            // Follow path steps recursively
            return directories[steps.first()]?.getFile(steps.drop(1))
        }

        fun getDirectory(path: String) = getDirectory(path.split('/'))

        fun getDirectory(path: List<String>): DirectoryNode? {
            // Ignore empty path segments
            val steps = path.dropWhile(String::isEmpty)

            // End of path
            steps.ifEmpty { return this }

            // Follow path steps recursively
            return directories[steps.first()]?.getDirectory(steps.drop(1))
        }

        fun listFiles() = files.values.toList()

        fun listDirectories() = directories.values.toList()

        fun listChildren() = listDirectories() + listFiles()

        fun allPaths(): List<String> {
            val d = listDirectories().flatMap { it.allPaths() }
            val f = listFiles().map { it.asPath() }
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
            directories[name]?.run {
                throw IllegalArgumentException("""A directory named "${asPath()}" already exists.""")
            }

            return files.compute(name) { _, value ->
                value?.run {
                    throw IllegalArgumentException("""A file named "${asPath()}" already exists.""")
                }

                FileNode(parent = this, name = name, supplier = supplier)
            }!!
        }

        private fun makeDirectoryNode(name: String): DirectoryNode {
            files[name]?.run {
                throw IllegalArgumentException("""A file named "${asPath()}" already exists.""")
            }

            return directories.computeIfAbsent(name) {
                DirectoryNode(parent = this, name = name)
            }
        }

    }
}