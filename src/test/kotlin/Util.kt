import java.io.File
import java.net.URLClassLoader

/**
 * @return a [File] pointing to the specified resource file, or `null`.
 */
fun resource(path: String): File? = URLClassLoader.getSystemResource(path)?.run { File(toURI()) }
