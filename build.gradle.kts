import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java

    //kotlin
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("plugin.spring") apply false
    id("org.jetbrains.kotlin.plugin.allopen") apply false

    //Other
    id("org.springframework.boot") apply false
    id("com.gorylenko.gradle-git-properties") apply false
    id("com.google.cloud.tools.jib") apply false
}

buildscript {
    dependencies {
        classpath("com.squareup:kotlinpoet:1.7.2")
    }
}

allprojects {
    //Project props
    group = "org.dreamexposure.discal"
    version = "4.2.1-SNAPSHOT"
    description = "DisCal"

    //Plugins
    apply(plugin = "java")
    apply(plugin = "kotlin")

    //Compiler nonsense
    java.sourceCompatibility = JavaVersion.VERSION_16
    java.targetCompatibility = JavaVersion.VERSION_16

    //Versions
    val kotlinVersion: String by properties
    val kotlinxSerializationVersion: String by properties

    val springVersion: String by properties

    val googleCoreVersion: String by properties
    val googleCalendarVersion: String by properties

    val r2MysqlVersion: String by properties
    val r2PoolVersion: String by properties

    val nettyVersion: String by properties
    val reactorBomVersion: String by properties

    val slfVersion: String by properties
    val jsonVersion: String by properties
    val okHttpVersion: String by properties
    val discordWebhookVersion: String by properties
    val copyDownVersion: String by properties

    repositories {
        mavenCentral()

        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.spring.io/milestone")
        maven("https://jitpack.io")
    }

    dependencies {
        //Boms
        implementation(platform("io.projectreactor:reactor-bom:$reactorBomVersion"))

        //Kotlin Deps
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

        //Forced stuff
        //slf4j-api - Need to force this for logback to work. I dunno
        implementation("org.slf4j:slf4j-api:$slfVersion")
        //Netty - forced due to stores-redis:lettuce-core giving 4.1.38
        implementation("io.netty:netty-all:$nettyVersion")
        //Forcing reactor version
        implementation("io.projectreactor:reactor-core")

        //Google apis
        implementation("com.google.api-client:google-api-client:$googleCoreVersion")
        implementation("com.google.apis:google-api-services-calendar:$googleCalendarVersion")
        implementation("com.google.oauth-client:google-oauth-client-jetty:$googleCoreVersion") {
            exclude(group = "org.mortbay.jetty", module = "servlet-api")
        }
        //r2dbc
        implementation("dev.miku:r2dbc-mysql:$r2MysqlVersion") {
            exclude("io.netty", "*")
            exclude("io.projectreactor", "*")
            exclude("io.projectreactor.netty", "*")
        }
        implementation("io.r2dbc:r2dbc-pool:$r2PoolVersion")


        implementation("org.json:json:$jsonVersion")

        implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")

        implementation("club.minnced:discord-webhooks:$discordWebhookVersion")

        implementation("io.github.furstenheim:copy_down:$copyDownVersion")

        //Spring
        implementation("org.springframework.boot:spring-boot-starter-webflux:$springVersion")
        implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:$springVersion")
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
                jvmTarget = targetCompatibility
            }
        }
    }
}

