plugins {
    id("java")
    id("maven-publish")

    id("com.gradleup.shadow") version "9.4.2"
    id("xyz.jpenilla.run-paper") version "3.0.2" // Adds runServer and runMojangMappedServer tasks for testing
}

group = "net.synchthia"
version = "1.21-SNAPSHOT"
description = "Nebula"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/groups/public/")
    }

    maven {
        name = "sonatype-oss-repo"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        name = "velocity-external"
        url = uri("https://repo.william278.net/velocity/")
    }

    maven {
        name = "codemc"
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }

    maven {
        name = "startail-public"
        url = uri("https://maven.pkg.github.com/synchthia/pkg-startail-public")
        credentials {
            username = System.getenv("MVN_REPO_USERNAME")
            password = System.getenv("MVN_REPO_PASSWORD")
        }
    }
}

dependencies {
    implementation("net.synchthia:nebula-api:1.1-SNAPSHOT")

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    // BungeeCord
    compileOnly("net.md-5:bungeecord-api:1.20-R0.1-SNAPSHOT")

    // Velocity
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-proxy:3.3.0-SNAPSHOT")

    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    // Adventure
    implementation("net.kyori:adventure-api:4.17.0")

    // Cloud
    implementation("org.incendo:cloud-core:2.0.0-rc.2")
    implementation("org.incendo:cloud-annotations:2.0.0-rc.2")
    implementation("org.incendo:cloud-paper:2.0.0-beta.8")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.8")

    // ProtocolLib
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")

    // Protocol Buffers
    implementation("com.google.protobuf:protobuf-java:3.21.9")
    implementation("com.google.protobuf:protobuf-java-util:3.21.9")

    // gRPC
    implementation("io.grpc:grpc-netty:1.64.0")
    implementation("io.grpc:grpc-protobuf:1.64.0")
    implementation("io.grpc:grpc-stub:1.64.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    // Redis
    implementation("redis.clients:jedis:5.1.3")

    // gson
    compileOnly("com.google.code.gson:gson:2.11.0")

    // lombok
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks {
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    shadowJar {
        archiveFileName.set("${project.name}.jar")
        mergeServiceFiles()
        fun reloc(pkg: String) = relocate(pkg, "net.synchthia.nebula.libs.$pkg")

        reloc("redis.clients")
        reloc("org.apache.commons")
        reloc("com.google.protobuf")
        reloc("org.apache.commons.pool2")
        reloc("io.grpc")
        reloc("io.netty")
    }

    assemble {
        dependsOn(shadowJar)
    }

    processResources {
        expand(
            mapOf(
                "pluginName" to "Nebula",
                "pluginVersion" to project.version,
                "description" to project.description,
                "authors" to "SYNCHTHIA",
                "website" to "https://synchthia.net",
                "bukkitApiVersion" to "1.20",
                "bukkitPluginMain" to "net.synchthia.nebula.bukkit.NebulaPlugin",
                "bungeePluginMain" to "net.synchthia.nebula.bungee.NebulaPlugin"
            )
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.shadowJar)
        }
    }

    repositories {
        maven {
            name = "startail-public"
            url = uri("https://maven.pkg.github.com/synchthia/pkg-startail-public")
            credentials {
                username = System.getenv("MVN_REPO_USERNAME")
                password = System.getenv("MVN_REPO_PASSWORD")
            }
        }
    }
}
