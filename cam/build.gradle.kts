plugins {
    // Kotlin
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.plugin.allopen")

    // Spring
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    // Tooling
    id("com.google.cloud.tools.jib")
}

dependencies {
    api(project(":core"))
}

kotlin {
    sourceSets {
        all {
            kotlin.srcDir("cam/src/main/kotlin")
        }
    }
}

jib {
    var imageVersion = version.toString()
    if (imageVersion.contains("SNAPSHOT")) imageVersion = "latest"

    to.image = "rg.nl-ams.scw.cloud/dreamexposure/discal-cam:$imageVersion"
    val baseImage: String by properties
    from.image = baseImage

    container.creationTime = "USE_CURRENT_TIMESTAMP"
}

tasks {
    bootJar {
        archiveFileName.set("DisCal-Cam.jar")
    }
}
