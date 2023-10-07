pluginManagement {
    val kotlinVersion: String by settings
    val springVersion: String by settings
    val gitPropertiesVersion: String by settings
    val jibVersion: String by settings
    val springDependencyManagementVersion: String by settings

    repositories {
        gradlePluginPortal()
    }

    plugins {
        // Kotlin
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion

        // Spring
        kotlin("plugin.spring") version kotlinVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("org.springframework.boot") version springVersion

        // Tooling
        id("com.gorylenko.gradle-git-properties") version gitPropertiesVersion apply false
        id("com.google.cloud.tools.jib") version jibVersion apply false
    }
}

rootProject.name = "DisCal"

include("core", "client", "server", "web", "cam")
