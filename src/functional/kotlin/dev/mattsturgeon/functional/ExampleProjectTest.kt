package dev.mattsturgeon.functional

import dev.mattsturgeon.extensions.childDirectories
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestMethodOrder
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@TestMethodOrder(OrderAnnotation::class)
class ExampleProjectTest {

    private var examples = File("examples").childDirectories()

    @BeforeTest
    fun init() {}

    @TestFactory
    @Order(1) // Run first so we have a cached build
    fun `Can build example`(): List<DynamicTest> {
        return examples.map {
            dynamicTest(it.name) {
                cleanBuildDir(it)
                GradleRunner.create()
                    .withProjectDir(it)
                    .withArguments("build", "--stacktrace", "--info", "--refresh-dependencies")
                    .withGradleVersion(GRADLE_CURRENT)
                    .build()
            }
        }
    }

    @TestFactory
    @Order(2) // Run after build to avoid re-building
    fun `Can run example`(): List<DynamicTest> {
        return examples
            .filter {
                it.resolve("expected_output").exists()
            }
            .map {
            dynamicTest(it.name) {
                val expectation = it.resolve("expected_output").readText()

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