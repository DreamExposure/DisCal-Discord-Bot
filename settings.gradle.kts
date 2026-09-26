pluginManagement {
    val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
    val springVersion = providers.gradleProperty("springVersion").get()
    val gitPropertiesVersion = providers.gradleProperty("gitPropertiesVersion").get()
    val jibVersion = providers.gradleProperty("jibVersion").get()
    val springDependencyManagementVersion = providers.gradleProperty("springDependencyManagementVersion").get()

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
