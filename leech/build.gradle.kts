val kord_version: String by project
val ktor_version: String by project
val logback_version: String by project

plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":shared"))
    implementation("dev.kord:kord-core:$kord_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
}

application {
    // Define the main class for the application.
    mainClass.set("bl.deflecc.freqcord.leech.AppKt")
}

group = "bl.deflecc"
