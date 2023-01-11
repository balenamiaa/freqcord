plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.exposed:exposed-core:0.40.1")
                implementation("org.jetbrains.exposed:exposed-java-time:0.40.1")
            }
        }
    }
}