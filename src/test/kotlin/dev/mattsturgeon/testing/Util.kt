package dev.mattsturgeon.testing

import java.io.File
import java.net.URLClassLoader
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * @return a [File] pointing to the specified resource file, or `null`.
 */
fun resource(path: String): File? = URLClassLoader.getSystemResource(path)?.run { File(toURI()) }

/**
 * Build a [temp][File.createTempFile] zip archive of the specified [directory][dir].
 *
 * @param dir the directory to archive
 * @param path the path _within_ the archive to copy [dir]'s content
 * @return a temp [ZipFile]
 */
fun makeZip(dir: File, path: String = ""): ZipFile {
    val temp = File.createTempFile(dir.name, "zip")

    ZipOutputStream(temp.outputStream().buffered()).use { zip ->
        dir.walk().filter(File::isFile).forEach { file ->
            zip.putNextEntry(ZipEntry("$path/${file.relativeTo(dir)}"))
            file.inputStream().buffered().copyTo(zip, 1024)
        }
    }

    return ZipFile(temp)
}
