import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    //kotlin
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.plugin.allopen")

    // Spring
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    //Tooling
    id("com.gorylenko.gradle-git-properties") apply false
    id("com.google.cloud.tools.jib") apply false
}

buildscript {
    val kotlinPoetVersion: String by properties
    dependencies {
        classpath("com.squareup:kotlinpoet:$kotlinPoetVersion")
    }
}

allprojects {
    //Project props
    group = "org.dreamexposure.discal"
    version = "4.2.6"
    description = "DisCal"

    //Plugins
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")

    // Versions --- found in gradle.properties
    // Discord
    val discord4jVersion: String by properties
    val discord4jStoresVersion: String by properties
    val discordWebhookVersion: String by properties
    // Database\
    val mikuR2dbcMySqlVersion: String by properties
    val mySqlConnectorJava: String by properties
    // Serialization
    val kotlinxSerializationJsonVersion: String by properties
    // Observability
    val logbackContribVersion: String by properties
    // Google libs
    val googleApiClientVersion: String by properties
    val googleServicesCalendarVersion: String by properties
    val googleOauthClientVersion: String by properties
    // Various libs
    val copyDownVersion: String by properties
    val jsoupVersion: String by properties

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://repo.maven.apache.org/maven2/")
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.spring.io/milestone")
        maven("https://jitpack.io")
    }

    dependencies {
        // Tools
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

        // Discord
        implementation("com.discord4j:discord4j-core:$discord4jVersion")
        implementation("com.discord4j:stores-redis:$discord4jStoresVersion")
        implementation("club.minnced:discord-webhooks:$discordWebhookVersion")

        // Spring
        implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
        implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
        implementation("org.springframework.boot:spring-boot-starter-data-redis")
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-starter-cache")
        implementation("org.springframework.boot:spring-boot-starter-actuator")

        // Database
        implementation("dev.miku:r2dbc-mysql:$mikuR2dbcMySqlVersion")
        implementation("mysql:mysql-connector-java:$mySqlConnectorJava")

        // Serialization
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
        implementation("org.json:json")

        // Observability
        implementation("ch.qos.logback.contrib:logback-json-classic:$logbackContribVersion")
        implementation("ch.qos.logback.contrib:logback-jackson:$logbackContribVersion")
        implementation("io.micrometer:micrometer-registry-prometheus")

        // Google libs
        implementation("com.google.api-client:google-api-client:$googleApiClientVersion")
        implementation("com.google.apis:google-api-services-calendar:$googleServicesCalendarVersion")
        implementation("com.google.oauth-client:google-oauth-client-jetty:$googleOauthClientVersion") {
            exclude(group = "org.mortbay.jetty", module = "servlet-api")
        }

        // Various Libs
        implementation("com.squareup.okhttp3:okhttp")
        implementation("io.github.furstenheim:copy_down:$copyDownVersion")
        implementation("org.jsoup:jsoup:$jsoupVersion")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        sourceSets {
            all {
                languageSettings {
                    optIn("kotlinx.serialization.ExperimentalSerializationApi")
                    optIn("kotlin.RequiresOptIn")
                }
            }
        }
    }
}

subprojects {
    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = java.targetCompatibility.majorVersion
            }
        }
    }
}

tasks {
    wrapper {
        distributionType = ALL
        gradleVersion = "8.2.1"
    }

    bootJar {
        enabled = false
    }
}

