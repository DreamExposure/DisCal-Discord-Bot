val springVersion: String by ext
val springSecurityVersion: String by ext
val springSessionVersion: String by ext
val springR2Version: String by ext

dependencies {
    implementation(project(":core"))

    implementation("org.springframework.session:spring-session-data-redis:$springSessionVersion")
    implementation("org.springframework.security:spring-security-core:$springSecurityVersion")
    implementation("org.springframework.security:spring-security-web:$springSecurityVersion")
    implementation("org.springframework:spring-r2dbc:$springR2Version")
}

tasks.create<Exec>("buildDockerImage") {
    commandLine("sh", "build-image.sh", "$version")
}

tasks {
    build {
        if (System.getenv("BUILD_NUMBER") != null) {
            finalizedBy("buildDockerImage")
        }
    }
}
