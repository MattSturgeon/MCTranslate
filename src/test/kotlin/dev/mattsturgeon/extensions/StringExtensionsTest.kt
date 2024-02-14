package dev.mattsturgeon.extensions

import kotlin.test.Test
import kotlin.test.assertEquals

class StringExtensionsTest {

    private val expectations = listOf(
        NameExpectation("some_name", "some_name", ""),
        NameExpectation("some.name", "some", "name"),
        NameExpectation("some.complex.name", "some.complex", "name"),
        NameExpectation(
            "Stupid_name-=+qwaef9uh34,;.:gw3re8s7.hudaf\\sd£$\"RQ_.ext_54!",
            "Stupid_name-=+qwaef9uh34,;.:gw3re8s7.hudaf\\sd£$\"RQ_",
            "ext_54!"
        ),
    )

    @Test
    fun `basename() works correctly`() {
        expectations.forEach { (name, basename, _) ->
            println("Checking \"${name}\".basename() == \"${basename}\"")
            assertEquals(basename, name.basename())
        }
    }

    @Test
    fun `extension() works correctly`() {
        expectations.forEach { (name, _, extension) ->
            println("Checking \"${name}\".extension() == \"${extension}\"")
            assertEquals(extension, name.extension())
        }
    }

    private data class NameExpectation(
        val name: String,
        val basename: String,
        val extension: String
    )
}