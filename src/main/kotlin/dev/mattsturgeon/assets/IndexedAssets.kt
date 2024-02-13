package dev.mattsturgeon.assets

import kotlinx.serialization.json.Json
import java.io.Reader
import java.util.function.Supplier

open class IndexedAssets(index: Map<String, Supplier<Reader>>) : Assets {

    /**
     * A virtual directory tree
     */
    private val root = DirectoryNode("")

    init {
        // Load index into a directory tree
        index.forEach { (path, supplier) ->
            // Split path into steps
            val steps = path.split('/').filter(String::isNotEmpty)

            // Follow path's steps down tree, then add the file as a leaf node
            steps.dropLast(1)
                .fold(root) { dir, name -> dir.makeDirectory(name) }
                .makeFile(steps.last(), supplier)
        }
    }

    override fun packMeta() = root.get("pack.mcmeta")?.let {
        val text = it.supplier.get().readText()
        Json.decodeFromString<PackMeta>(text)
    }

    override fun getLang(lang: String): Map<String, String>? {
        return root.directories.asSequence()
            // Search all top-level dirs for "lang" sub-dirs
            .mapNotNull { it.getDirectory("lang") }
            // Then get all files matching the requested lang
            .flatMap { it.files }
            .filter { lang == it.basename() }
            // And parse them using Language
            .map { Language.parse(it.name, it.supplier.get()) }
            .map { it.translations }
            // Finally, combine all parsed files into one Map
            .reduceOrNull(Map<String, String>::plus)
    }

    internal interface Node {
        val name: String
        val parent: Node?

        fun basename(): String = name.substringBeforeLast('.')

        fun asPath(): String {
            val parentPath = parent?.asPath()?.plus("/") ?: ""
            return parentPath + name
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
        fun get(vararg path: String): FileNode? {
            return when (path.size) {
                0 -> null
                1 -> files.firstOrNull { it.name == path[0] }
                else -> {
                    val dirName = path.first()
                    val subPath = path.drop(1).toTypedArray()
                    directories.firstOrNull { it.name == dirName }?.get(*subPath)
                }
            }
        }

        fun getDirectory(vararg path: String): DirectoryNode? {
            if (path.isEmpty()) {
                return this
            }
            val dirName = path.first()
            val subPath = path.drop(1).toTypedArray()
            return directories.firstOrNull { it.name == dirName }?.getDirectory(*subPath)
        }

        fun makeDirectory(name: String): DirectoryNode {
            val existing = directories.firstOrNull() { it.name == name }
            return existing ?: run {
                val node = DirectoryNode(parent = this, name = name)
                directories.add(node)
                node
            }
        }

        fun makeFile(name: String, supplier: Supplier<Reader>): FileNode {
            files.firstOrNull { it.name == name }?.run {
                throw IllegalArgumentException("""Cannot add duplicate file "$name" to "${asPath()}".""")
            }

            val node = FileNode(parent = this, name = name, supplier = supplier)
            files.add(node)
            return node
        }
    }
}