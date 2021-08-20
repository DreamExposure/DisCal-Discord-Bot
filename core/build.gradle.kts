val discord4jVersion: String by ext
val discord4jStoresVersion: String by ext

dependencies {
    implementation("com.discord4j:discord4j-core:$discord4jVersion") {
        exclude(group = "io.projectreactor.netty", module = "*")
    }
    implementation("com.discord4j:stores-redis:$discord4jStoresVersion") {
        exclude(group = "io.netty", module = "*")
        exclude(group = "io.projectreactor.netty", module = "*")
    }
}

tasks.create<Exec>("pruneImages") {
        commandLine("podman", "image", "prune", "-a", "-f")
}

tasks {
    clean {
        if (System.getenv("BUILD_NUMBER") != null)
        dependsOn("pruneImages")
    }
}
