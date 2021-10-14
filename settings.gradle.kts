pluginManagement {
    val kotlinVersion: String by settings
    val springVersion: String by settings
    val gitPropsVersion: String by settings
    val jibVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion

        id("org.springframework.boot") version springVersion apply false
        id("com.gorylenko.gradle-git-properties") version gitPropsVersion apply false
        id("com.google.cloud.tools.jib") version jibVersion apply false
    }

    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "DisCal"

include("core", "client", "server", "web")
