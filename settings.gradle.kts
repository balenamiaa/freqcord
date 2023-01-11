rootProject.name = "freqcord"
include(
    ":leech",
    ":backend",
    ":shared",
    ":frontend"
)


plugins {
    kotlin("js") version "1.7.22" apply false
    kotlin("multiplatform") version "1.7.22" apply false
    kotlin("plugin.serialization") version "1.7.22" apply false
    id("org.jetbrains.kotlin.jvm") version "1.7.22" apply false
    id("io.ktor.plugin") version "2.2.1" apply false

    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion apply false
}