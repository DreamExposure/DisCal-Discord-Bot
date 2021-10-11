plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("com.google.cloud.tools.jib")
}

val springVersion: String by ext
val springSecurityVersion: String by ext
val springSessionVersion: String by ext
val springR2Version: String by ext

dependencies {
    api(project(":core"))

    //Database stuff
    implementation("org.flywaydb:flyway-core:7.11.2")
    implementation("mysql:mysql-connector-java:8.0.25")
    implementation("com.zaxxer:HikariCP:5.0.0")

    //Top gg lib
    implementation("org.discordbots:DBL-Java-Library:2.0.1")

    //Spring libs
    implementation("org.springframework.session:spring-session-data-redis:$springSessionVersion")
    implementation("org.springframework.security:spring-security-core:$springSecurityVersion")
    implementation("org.springframework.security:spring-security-web:$springSecurityVersion")
    implementation("org.springframework:spring-r2dbc:$springR2Version")

    //jackson for kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.4")
}

kotlin {
    sourceSets {
        all {
            kotlin.srcDir("server/src/main/kotlin")
        }
    }
}

jib {
    var imageVersion = version.toString()
    if (imageVersion.contains("SNAPSHOT")) imageVersion = "latest"

    to.image = "rg.nl-ams.scw.cloud/dreamexposure/discal-server:$imageVersion"
    from.image = "adoptopenjdk/openjdk16:alpine-jre"
    container.creationTime = "USE_CURRENT_TIMESTAMP"
}

tasks {
    bootJar {
        archiveFileName.set("DisCal-Server.jar")
    }
}
