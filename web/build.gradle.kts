import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("com.google.cloud.tools.jib")
}

val thymeleafVersion: String by properties
val thymeleafSecurityVersion: String by properties
val thymeleafLayoutVersion: String by properties

val springVersion: String by properties
val springSecurityVersion: String by properties
val springSessionVersion: String by properties
val springR2Version: String by properties

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
    val baseImage: String by properties
    from.image = baseImage

    container.creationTime = "USE_CURRENT_TIMESTAMP"
}

// The weird OS checks are because of windows. See this SO answer: https://stackoverflow.com/a/53428540

tasks {
    create<Exec>("npm") {
        var npm = "npm"
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            npm = "npm.cmd"
        }

        workingDir("..")
        commandLine(npm, "ci")
    }

    create<Exec>("cleanWeb") {
        var gulp = "gulp"
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            gulp = "gulp.cmd"
        }
        commandLine(gulp, "clean:all")
    }

    create<Exec>("compileCSS") {
        var gulp = "gulp"
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            gulp = "gulp.cmd"
        }

        commandLine(gulp, "build")
    }

    create<Exec>("compileTypescript") {
        var webpack = "webpack"
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            webpack = "webpack.cmd"
        }

        workingDir("..")
        commandLine(webpack)
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
