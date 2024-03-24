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

dependencies {
    api(project(":core"))
}

kotlin {
    sourceSets {
        all {
            kotlin.srcDir("client/src/main/kotlin")
        }
    }
}

jib {
    to {
        val buildVersion = if (System.getenv("GITHUB_RUN_NUMBER") != null) {
            "$version.b${System.getenv("GITHUB_RUN_NUMBER")}"
        } else {
            "$version.d${System.currentTimeMillis().div(1000)}" //Seconds since epoch
        }

        image = "rg.nl-ams.scw.cloud/dreamexposure/discal-client"
        tags = mutableSetOf("latest", buildVersion)
    }

    val baseImage: String by properties
    from.image = baseImage
}

tasks {
    bootJar {
        archiveFileName.set("DisCal-Client.jar")
    }
}
