package dev.mattsturgeon.functional

import dev.mattsturgeon.extensions.childDirectories
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import kotlin.test.assertEquals

class ExampleProjectTest {

    private var examples = File("examples").childDirectories()

    @TestFactory
    fun `Prints expected output`(): List<DynamicTest> {
        return examples
            .filter {
                it.resolve("expected_output").exists()
            }
            .map {
                dynamicTest(it.name) {
                    val expectation = it.resolve("expected_output").readText()

                    cleanBuildDir(it)
                    val result = GradleRunner.create()
                        .withProjectDir(it)
                        .withArguments("run", "--quiet")
                        .withGradleVersion(GRADLE_CURRENT)
                        .build()

                    assertEquals(expectation, result.output)
                }
            }
    }
}