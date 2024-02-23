package dev.mattsturgeon.extensions

/**
 * True when this iterable starts with provided iterable.
 *
 * Always `true` when [prefix] is empty.
 *
 * Always `false` when [prefix] is larger than [this].
 */
fun <T> Iterable<T>.startsWith(prefix: Iterable<T>): Boolean {
    // Minor optimisation for large collections:
    // Check for a size mismatch before comparing elements
    if (this is Collection && prefix is Collection) {
        if (prefix.size > size) {
            return false
        }
    }

    val self = iterator()
    val other = prefix.iterator()

    while (other.hasNext()) {
        if (!self.hasNext()) {
            return false
        }
        if (self.next() != other.next()) {
            return false
        }
    }

    return true
}
