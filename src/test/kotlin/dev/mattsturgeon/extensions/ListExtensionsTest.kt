package dev.mattsturgeon.extensions

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ListExtensionsTest {

    @Test
    fun `can handle empties`() {
        val emptySequence = sequenceOf<String>().asIterable()
        val emptyList = emptyList<String>()

        assertTrue(emptyList.startsWith(emptySequence))
        assertTrue(emptySequence.startsWith(emptyList))
        assertTrue(emptyList.startsWith(emptyList))
    }

    @Test
    fun `non-empty starts with empty`() {
        val emptySequence = sequenceOf<String>().asIterable()
        val emptyList = emptyList<String>()
        val nonEmpty = sequenceOf("foo").asIterable()
        val nonEmptyList = listOf("foo")

        assertTrue(nonEmpty.startsWith(emptySequence))
        assertTrue(nonEmptyList.startsWith(emptyList))
    }

    @Test
    fun `starts with self`() {
        val a = listOf("foo")
        val b = listOf("foo", "bar")

        assertTrue(a.startsWith(a))
        assertTrue(b.startsWith(b))
    }

    @Test
    fun `not starts with longer collection`() {
        val a = listOf("foo")
        val b = listOf("foo", "bar")

        assertFalse(a.startsWith(b))
    }

    @Test
    fun `not starts with longer iterable`() {
        val a = sequenceOf("foo").asIterable()
        val b = sequenceOf("foo", "bar").asIterable()

        assertFalse(a.startsWith(b))
    }

    @Test
    fun `starts with prefix`() {
        val a = listOf("foo")
        val b = listOf("foo", "bar")

        assertTrue(b.startsWith(a))
    }
}