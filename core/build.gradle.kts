import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    // Kotlin
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.plugin.allopen")

    // Spring
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    // Tooling
    id("com.gorylenko.gradle-git-properties")
}

val discord4jVersion: String by properties
val kotlinSrcDir: File = buildDir.resolve("core/src/main/kotlin")

kotlin {
    sourceSets {
        all {
            kotlin.srcDir(kotlinSrcDir)
        }
    }
}

gitProperties {
    extProperty = "gitPropertiesExt"

    val versionName = if (System.getenv("GITHUB_RUN_NUMBER") != null) {
        "$version.b${System.getenv("GITHUB_RUN_NUMBER")}"
    } else {
        "$version.d${System.currentTimeMillis().div(1000)}" //Seconds since epoch
    }

    // Custom git properties for compile-time constants
    customProperty("discal.version", versionName)
    customProperty("discal.version.d4j", discord4jVersion)
}

tasks {
    generateGitProperties {
        doLast {
            @Suppress("UNCHECKED_CAST")
            val gitProperties = ext[gitProperties.extProperty] as Map<String, String>
            val enumPairs = gitProperties.mapKeys { it.key.replace('.', '_').uppercase(Locale.getDefault()) }

            val enumBuilder = TypeSpec.enumBuilder("GitProperty")
                  .primaryConstructor(
                        FunSpec.constructorBuilder()
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
    }
}
