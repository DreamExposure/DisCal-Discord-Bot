plugins {
    kotlin("jvm") version "1.5.21"
    java

    kotlin("plugin.spring") version "1.5.21"
    id ("org.springframework.boot") version ("2.5.2")
}

//Versions
val revision by ext("4.1.2-SNAPSHOT")


val kotlinVersion by ext("1.5.21")
val kotlinxSerializationVersion by ext("1.2.2")

val discord4jVersion by ext("3.2.0-SNAPSHOT")
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

val kotlinSrcDir: File = buildDir.resolve("src/main/kotlin")

//Project props
group = "org.dreamexposure.discal"
version = revision
description = "DisCal"
java.sourceCompatibility = JavaVersion.VERSION_16

allprojects {
    apply(plugin = "java")
    repositories {
        mavenCentral()

        maven("https://emily.dreamexposure.org/artifactory/dreamexposure-public/")
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.spring.io/milestone")
        maven("https://jitpack.io")
    }

    dependencies {
        implementation(platform("io.projectreactor:reactor-bom:$reactorBomVersion"))
    }
}

kotlin {
    sourceSets {
        all {
            kotlin.srcDir(kotlinSrcDir)
        }
    }
}
