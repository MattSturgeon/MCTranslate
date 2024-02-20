plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
}

group = "dev.mattsturgeon"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

sourceSets {
    register("indexer") {
        compileClasspath += main.get().compileClasspath
        runtimeClasspath += main.get().runtimeClasspath
    }
    register("functional") {
        compileClasspath += test.get().compileClasspath
        runtimeClasspath += test.get().runtimeClasspath
    }
}

val indexerImplementation: Configuration by configurations.getting

val functionalImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    indexerImplementation(sourceSets.main.get().output)
    indexerImplementation("com.github.ajalt.clikt:clikt:4.2.2")

    testImplementation(kotlin("test"))
    functionalImplementation(gradleTestKit())
}

tasks.test {
    useJUnitPlatform()
}

val functionalTest by tasks.registering(Test::class) {
    description = "Runs the functional test suite."
    group = "verification"

    testClassesDirs = sourceSets["functional"].output.classesDirs
    classpath = sourceSets["functional"].runtimeClasspath
    shouldRunAfter("test")
    useJUnitPlatform()
}

tasks.check {
    dependsOn(functionalTest)
}

tasks.wrapper {
    version = "8.5"
    distributionType = Wrapper.DistributionType.ALL
}

kotlin {
    jvmToolchain(17)
}