import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("com.google.cloud.tools.jib")
}

val thymeleafVersion: String by ext
val thymeleafSecurityVersion: String by ext
val thymeleafLayoutVersion: String by ext

val springVersion: String by ext
val springSecurityVersion: String by ext
val springSessionVersion: String by ext
val springR2Version: String by ext

dependencies {
    api(project(":core"))

    //Thymeleaf
    implementation("org.thymeleaf:thymeleaf:$thymeleafVersion")
    implementation("org.thymeleaf:thymeleaf-spring5:$thymeleafVersion")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:$thymeleafLayoutVersion")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5:$thymeleafSecurityVersion")

    //Spring
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf:$springVersion")
    implementation("org.springframework.session:spring-session-data-redis:$springSessionVersion")
    implementation("org.springframework.security:spring-security-core:$springSecurityVersion")
    implementation("org.springframework.security:spring-security-web:$springSecurityVersion")
}

kotlin {
    sourceSets {
        all {
            kotlin.srcDir("web/src/main/kotlin")
        }
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/main/html")
        }
    }
}

jib {
    var imageVersion = version.toString()
    if (imageVersion.contains("SNAPSHOT")) imageVersion = "latest"

    to.image = "rg.nl-ams.scw.cloud/dreamexposure/discal-web:$imageVersion"
    from.image = "adoptopenjdk/openjdk16:alpine-jre"

    container.creationTime = "USE_CURRENT_TIMESTAMP"
}

tasks {
    create<Exec>("npm") {
        workingDir("..")
        commandLine("npm", "ci")
    }

    create<Exec>("cleanWeb") {
        commandLine("gulp", "clean:all")
    }

    create<Exec>("compileCSS") {
        commandLine("gulp", "build")
    }

    create<Exec>("compileTypescript") {
        workingDir("..")
        commandLine("webpack")
    }

    clean {
        dependsOn("npm")
        finalizedBy("cleanWeb")
    }

    withType<KotlinCompile> {
        dependsOn("compileCSS", "compileTypescript")
    }

    bootJar {
        archiveFileName.set("DisCal-Web.jar")
    }
}
