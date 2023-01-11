val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.ktor.plugin")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.jetbrains.exposed:exposed-core:0.40.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.40.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.40.1")
    implementation("com.h2database:h2:2.1.214")
    implementation("org.jetbrains.exposed:exposed-java-time:0.40.1")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.ehcache:ehcache:3.10.6")
    implementation(project(":shared"))
    implementation("io.ktor:ktor-server-cors-jvm:2.2.1")
}

application {
    mainClass.set("bl.deflecc.freqcord.backend.AppKt")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

group = "bl.deflecc"