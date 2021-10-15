plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("com.google.cloud.tools.jib")
}

val springVersion: String by properties
val springSecurityVersion: String by properties
val springSessionVersion: String by properties
val springR2Version: String by properties
val flywayVersion: String by properties
val mysqlConnectorVersion: String by properties
val hikariVersion: String by properties
val jacksonKotlinModVersion: String by properties

dependencies {
    api(project(":core"))

    //Database stuff
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("mysql:mysql-connector-java:$mysqlConnectorVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")

    //Spring libs
    implementation("org.springframework.session:spring-session-data-redis:$springSessionVersion")
    implementation("org.springframework.security:spring-security-core:$springSecurityVersion")
    implementation("org.springframework.security:spring-security-web:$springSecurityVersion")
    implementation("org.springframework:spring-r2dbc:$springR2Version")

    //jackson for kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonKotlinModVersion")
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
    val baseImage: String by properties
    from.image = baseImage

    container.creationTime = "USE_CURRENT_TIMESTAMP"
}

tasks {
    bootJar {
        archiveFileName.set("DisCal-Server.jar")
    }
}
