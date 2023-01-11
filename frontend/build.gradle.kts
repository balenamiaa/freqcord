import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("js")
    kotlin("plugin.serialization")
    id("io.kvision")
}

group = "bl.deflecc"
version = "1.0-SNAPSHOT"
val kvisionVersion: String by System.getProperties()

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

val webDir = file("src/main/web")

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            runTask {
                outputFileName = "main.bundle.js"
                sourceMaps = false
                devServer = KotlinWebpackConfig.DevServer(
                    open = false,
                    port = 3000,
                    proxy = mutableMapOf(
                        "/kv/*" to "http://localhost:8080",
                        "/kvws/*" to mapOf("target" to "ws://localhost:8080", "ws" to true)
                    ),
                    static = mutableListOf("$buildDir/processedResources/js/main")
                )
            }
            webpackTask {
                outputFileName = "main.bundle.js"
            }
        }
    }

    sourceSets["main"].dependencies {
        val ktor_version: String by project
        val echarts_version: String by project

        implementation(project(":shared"))
        implementation("io.kvision:kvision:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-css:$kvisionVersion")
        implementation("io.kvision:kvision-i18n:$kvisionVersion")
        implementation("io.kvision:kvision-redux-kotlin:$kvisionVersion")
        implementation("io.kvision:kvision-state:$kvisionVersion")
        implementation("io.kvision:kvision-routing-navigo:$kvisionVersion")

        implementation("io.ktor:ktor-client-core:$ktor_version")
        implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

        implementation(npm("echarts", echarts_version))
    }
    sourceSets["main"].resources.srcDir(webDir)
}