import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java

    //kotlin
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31" apply false
    id("org.jetbrains.kotlin.plugin.allopen") version "1.5.31" apply false

    //Other
    id("org.springframework.boot") version ("2.5.2") apply false
    id("com.gorylenko.gradle-git-properties") version "2.3.1" apply false
    id("com.google.cloud.tools.jib") version "3.1.4" apply false
}

buildscript {
    dependencies {
        classpath("com.squareup:kotlinpoet:1.7.2")
    }
}

val discord4jVersion = "3.2.0" //Has to be here to show up in git properties task
@Suppress("UNUSED_VARIABLE")
allprojects {
    //Project props
    group = "org.dreamexposure.discal"
    version = "4.1.3-SNAPSHOT"
    description = "DisCal"

    //Plugins
    apply(plugin = "java")
    apply(plugin = "kotlin")

    //Compiler nonsense
    java.sourceCompatibility = JavaVersion.VERSION_16
    java.targetCompatibility = JavaVersion.VERSION_16

    //Versions
    val kotlinVersion by ext("1.5.31")
    val kotlinxSerializationVersion by ext("1.3.0-RC")

    val discord4jVersion by ext(discord4jVersion)
    val discord4jStoresVersion by ext("3.2.1")

    val thymeleafVersion by ext("3.0.12.RELEASE")
    val thymeleafSecurityVersion by ext("3.0.4.RELEASE")
    val thymeleafLayoutVersion by ext("2.5.3")

    val springVersion by ext("2.5.2")
    val springSecurityVersion by ext("5.5.1")
    val springSessionVersion by ext("2.5.1")
    val springR2Version by ext("5.3.9")

    val googleCoreVersion by ext("1.32.1")
    val googleCalendarVersion by ext("v3-rev20210708-1.32.1")
    val googleOauthVersion by ext("1.31.5")

    val r2MysqlVersion by ext("0.8.2.RELEASE")
    val r2PoolVersion by ext("0.8.7.RELEASE")

    val nettyVersion by ext("4.1.56.Final")
    val reactorBomVersion by ext("2020.0.8")


    repositories {
        mavenCentral()

        maven("https://emily.dreamexposure.org/artifactory/dreamexposure-public/")
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
        implementation("org.slf4j:slf4j-api:1.7.31")
        //Netty - forced due to stores-redis:lettuce-core giving 4.1.38
        implementation("io.netty:netty-all:$nettyVersion")
        //Forcing reactor version
        implementation("io.projectreactor:reactor-core")

        //Google apis
        implementation("com.google.api-client:google-api-client:$googleCoreVersion")
        implementation("com.google.apis:google-api-services-calendar:$googleCalendarVersion")
        implementation("com.google.oauth-client:google-oauth-client-jetty:$googleOauthVersion") {
            exclude(group = "org.mortbay.jetty", module = "servlet-api")
        }
        //r2dbc
        implementation("dev.miku:r2dbc-mysql:$r2MysqlVersion") {
            exclude("io.netty", "*")
            exclude("io.projectreactor", "*")
            exclude("io.projectreactor.netty", "*")
        }
        implementation("io.r2dbc:r2dbc-pool:$r2PoolVersion")


        implementation("org.json:json:20210307")

        implementation("com.squareup.okhttp3:okhttp:4.9.1")

        implementation("club.minnced:discord-webhooks:0.5.7")

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

