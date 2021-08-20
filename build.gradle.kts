
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    java

    kotlin("plugin.spring") version "1.5.21"
    id("org.springframework.boot") version ("2.5.2")

    id("com.gorylenko.gradle-git-properties") version "2.3.1"
}

buildscript {
    dependencies {
        classpath("com.squareup:kotlinpoet:1.7.2")
    }
}

val discord4jVersion = "3.2.0-SNAPSHOT" //Has to be here to show up in git properties task
allprojects {
    group = "org.dreamexposure.discal"
    version = "4.1.2-SNAPSHOT"
    description = "DisCal"

    apply(plugin = "java")

    java.sourceCompatibility = JavaVersion.VERSION_16


    //Versions
    val kotlinVersion by ext("1.5.21")
    val kotlinxSerializationVersion by ext("1.2.2")

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

        //Kotlinx Deps
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

        //Forced stuff
        //slf4j-api - Need to force this for logback to work. I dunno
        implementation("org.slf4j:slf4j-api:1.7.31")
        //Netty - forced due to stores-redis:lettuce-core giving 4.1.38
        implementation("io.netty:netty-all:$nettyVersion")
        //Forcing reactor version
        implementation("io.projectreactor:reactor-core")
        implementation("org.dreamexposure:NovaUtils:1.0.0-SNAPSHOT")

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
}

gitProperties {
    extProperty = "gitPropertiesExt"

    val versionName = if (System.getenv("BUILD_NUMBER") != null) {
        "$version.b${System.getenv("BUILD_NUMBER")}"
    } else {
        "$version.d${System.currentTimeMillis().div(1000)}" //Seconds since epoch
    }

    customProperty("discal.version", versionName)
    customProperty("discal.version.d4j", discord4jVersion)
}

val kotlinSrcDir: File = buildDir.resolve("src/main/kotlin")
kotlin {

    sourceSets {
        all {
            kotlin.srcDir(kotlinSrcDir)
            languageSettings {
                useExperimentalAnnotation("kotlinx.serialization.InternalSerializationApi")
            }
        }
    }
}

tasks {
    generateGitProperties {
        doLast {
            @Suppress("UNCHECKED_CAST")
            val gitProperties = ext[gitProperties.extProperty] as Map<String, String>
            val enumPairs = gitProperties.mapKeys { it.key.replace('.', '_').toUpperCase() }

            val enumBuilder = TypeSpec.enumBuilder("GitProperty")
                  .primaryConstructor(
                        com.squareup.kotlinpoet.FunSpec.constructorBuilder()
                              .addParameter("value", String::class)
                              .build()
                  )

            val enums = enumPairs.entries.fold(enumBuilder) { accumulator, (key, value) ->
                accumulator.addEnumConstant(
                      key, TypeSpec.anonymousClassBuilder()
                      .addSuperclassConstructorParameter("%S", value)
                      .build()
                )
            }

            val enumFile = FileSpec.builder("org.dreamexposure.discal", "GitProperty")
                  .addType(
                        enums // https://github.com/square/kotlinpoet#enums
                              .addProperty(
                                    PropertySpec.builder("value", String::class)
                                          .initializer("value")
                                          .build()
                              )
                              .build()
                  )
                  .build()

            enumFile.writeTo(kotlinSrcDir)
        }
    }

    withType<KotlinCompile> {
        dependsOn(generateGitProperties)

        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = targetCompatibility
        }
    }

    bootJar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
