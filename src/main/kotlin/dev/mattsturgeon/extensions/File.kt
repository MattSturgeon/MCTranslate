package dev.mattsturgeon.extensions

import java.io.File

fun File.childFiles() = listFiles { file -> file.isFile } ?: emptyArray()
fun File.childDirectories() = listFiles { file -> file.isDirectory } ?: emptyArray()
