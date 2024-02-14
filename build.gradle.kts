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
        dependencies.add(implementationConfigurationName, main.get().output)
        dependencies.add(implementationConfigurationName, "com.github.ajalt.clikt:clikt:4.2.2")
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.wrapper {
    version = "8.5"
    distributionType = Wrapper.DistributionType.ALL
}

kotlin {
    jvmToolchain(17)
}