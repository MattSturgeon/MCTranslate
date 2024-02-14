package dev.mattsturgeon.extensions

/**
 * Returns the substring before the last `'.'` or the entire string if it contains no `'.'`s.
 *
 * Does not split up path segments, so `"/some/path.ext".basename()` will evaluate to `"/some/path"`.
 */
fun String.basename(): String = substringBeforeLast('.')

/**
 * Returns the file extension, or an empty string.
 */
fun String.extension(): String = substringAfterLast('.', "")
