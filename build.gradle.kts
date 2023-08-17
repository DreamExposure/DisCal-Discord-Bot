import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java

    //kotlin
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.plugin.allopen")

    // Spring
    kotlin("plugin.spring")
    id("org.springframework.boot") apply false
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
    version = "4.2.3"
    description = "DisCal"

    //Plugins
    apply(plugin = "java")
    apply(plugin = "kotlin")

    //Compiler nonsense
    java.sourceCompatibility = JavaVersion.VERSION_16
    java.targetCompatibility = JavaVersion.VERSION_16

    // Versions --- found in gradle.properties
    val kotlinVersion: String by properties
    // Tool
    val kotlinxCoroutinesReactorVersion: String by properties
    val reactorKotlinExtensions: String by properties
    // Discord
    val discord4jVersion: String by properties
    val discord4jStoresVersion: String by properties
    val discordWebhookVersion: String by properties
    // Spring
    val springVersion: String by properties
    // Database
    val flywayVersion: String by properties
    val mikuR2dbcMySqlVersion: String by properties
    val mySqlConnectorJava: String by properties
    // Serialization
    val kotlinxSerializationJsonVersion: String by properties
    val jacksonVersion: String by properties
    val jsonVersion: String by properties
    // Google libs
    val googleApiClientVersion: String by properties
    val googleServicesCalendarVersion: String by properties
    val googleOauthClientVersion: String by properties
    // Various libs
    val okhttpVersion: String by properties
    val copyDownVersion: String by properties

    repositories {
        mavenCentral()
        mavenLocal()

        maven {
            url = uri("https://repo.maven.apache.org/maven2/")
        }

        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.spring.io/milestone")
        maven("https://jitpack.io")
    }

    dependencies {
        // Tools
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinxCoroutinesReactorVersion")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:$reactorKotlinExtensions")

        // Discord
        implementation("com.discord4j:discord4j-core:$discord4jVersion")
        implementation("com.discord4j:stores-redis:$discord4jStoresVersion")
        implementation("club.minnced:discord-webhooks:$discordWebhookVersion")

        // Spring
        implementation("org.springframework.boot:spring-boot-starter-data-jdbc:$springVersion")
        implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:$springVersion")
        implementation("org.springframework.boot:spring-boot-starter-data-redis:$springVersion")
        implementation("org.springframework.boot:spring-boot-starter-webflux:$springVersion")
        implementation("org.springframework.boot:spring-boot-starter-cache:$springVersion")

        // Database
        implementation("org.flywaydb:flyway-core:$flywayVersion")
        implementation("org.flywaydb:flyway-mysql:$flywayVersion")
        implementation("dev.miku:r2dbc-mysql:$mikuR2dbcMySqlVersion")
        implementation("mysql:mysql-connector-java:$mySqlConnectorJava")

        // Serialization
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
        implementation("org.json:json:$jsonVersion")

        // Google libs
        implementation("com.google.api-client:google-api-client:$googleApiClientVersion")
        implementation("com.google.apis:google-api-services-calendar:$googleServicesCalendarVersion")
        implementation("com.google.oauth-client:google-oauth-client-jetty:$googleOauthClientVersion") {
            exclude(group = "org.mortbay.jetty", module = "servlet-api")
        }

        // Various Libs
        implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
        implementation("io.github.furstenheim:copy_down:$copyDownVersion")
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
}

