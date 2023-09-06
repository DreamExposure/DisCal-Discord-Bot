import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Kotlin
    id("org.jetbrains.kotlin.plugin.allopen")

    // Spring
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    // Tooling
    id("com.google.cloud.tools.jib")
}

dependencies {
    api(project(":core"))

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
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
    to {
        image = "rg.nl-ams.scw.cloud/dreamexposure/discal-web"
        tags = mutableSetOf("latest", version.toString())
    }

    val baseImage: String by properties
    from.image = baseImage
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
        dependsOn("npm")
        var gulp = "gulp"
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            gulp = "gulp.cmd"
        }
        commandLine(gulp, "clean:all")
    }

    create<Exec>("compileCSS") {
        dependsOn("npm")
        var gulp = "gulp"
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            gulp = "gulp.cmd"
        }

        commandLine(gulp, "build")
    }

    create<Exec>("compileTypescript") {
        dependsOn("npm")
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
        dependsOn("npm", "compileCSS", "compileTypescript")
    }

    bootJar {
        archiveFileName.set("DisCal-Web.jar")
    }
}
