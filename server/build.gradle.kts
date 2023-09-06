plugins {
    // Kotlin
    id("org.jetbrains.kotlin.plugin.allopen")

    // Spring
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    // Tooling
    id("com.google.cloud.tools.jib")
}

// Versions --- found in gradle.properties
// Database
val flywayVersion: String by properties

dependencies {
    api(project(":core"))

    // Database
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-mysql:$flywayVersion")
}

kotlin {
    sourceSets {
        all {
            kotlin.srcDir("server/src/main/kotlin")
        }
    }
}

jib {
    to {
        image = "rg.nl-ams.scw.cloud/dreamexposure/discal-server"
        tags = mutableSetOf("latest", version.toString())
    }

    val baseImage: String by properties
    from.image = baseImage
}

tasks {
    bootJar {
        archiveFileName.set("DisCal-Server.jar")
    }
}
