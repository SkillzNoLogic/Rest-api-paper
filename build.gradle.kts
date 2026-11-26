plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

// Build target: which Minecraft line to compile against.
//   ./gradlew build -Ptarget=1.20   -> Java 17 bytecode, works on 1.20 - 26.x (universal)
//   ./gradlew build -Ptarget=1.21   -> Java 21 bytecode, works on 1.20.5 - 26.x
//   ./gradlew build -Ptarget=26     -> Java 25 bytecode, works on 26.x
data class Target(val paperApi: String, val javaRelease: Int, val apiVersion: String, val label: String)

val targets = mapOf(
    "1.20" to Target("1.20.1-R0.1-SNAPSHOT", 17, "1.20", "mc1.20-26.x"),
    "1.21" to Target("1.21.11-R0.1-SNAPSHOT", 21, "1.21", "mc1.21-26.x"),
    "26" to Target("26.3.build.262-stable", 25, "1.21", "mc26.x"),
)
val targetKey = (findProperty("target") as String?) ?: "1.20"
val target = targets[targetKey] ?: error("Unknown target '$targetKey', expected one of ${targets.keys}")

dependencies {
    compileOnly("io.papermc.paper:paper-api:${target.paperApi}")
    compileOnly("me.clip:placeholderapi:2.11.6")
    // Gson is bundled with every Paper/Spigot server, so it is not shaded.
    compileOnly("com.google.code.gson:gson:2.10.1")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    // Let a newer JDK compile against older APIs and vice versa; bytecode level is set by options.release.
    disableAutoTargetJvm()
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(target.javaRelease)
    }

    jar {
        archiveBaseName.set("RestStatsAPI")
        archiveClassifier.set(target.label)
    }

    runServer {
        minecraftVersion("1.21.11")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to project.version, "apiVersion" to target.apiVersion)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
