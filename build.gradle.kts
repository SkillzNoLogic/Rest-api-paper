import org.gradle.kotlin.dsl.support.serviceOf

// RestStatsAPI multi-platform build.
//   ./gradlew releaseJars   -> every platform jar in build/release/

plugins {
    id("xyz.jpenilla.run-paper") version "3.0.2" apply false
}

// Platform jars: project -> (jar label, Java bytecode level)
val platforms = mapOf(
    "bukkit" to ("bukkit-mc1.8-26.x" to 8),
    "spigot" to ("spigot-mc1.20-26.x" to 17),
    "paper" to ("paper-mc1.20-26.x" to 17),
    "purpur" to ("purpur-mc1.20-26.x" to 17),
    "fabric" to ("fabric-mc1.20-26.x" to 17),
    "forge" to ("forge-mc1.20-26.x" to 17),
)
// Shared libraries are compiled for Java 8 so the universal Bukkit jar can use them.
val libraries = setOf("core", "bukkit-common", "vanilla")

subprojects {
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.purpurmc.org/snapshots")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://libraries.minecraft.net/")
    }

    configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
        // Bytecode level is controlled by options.release; let Gradle resolve APIs built for any Java version.
        disableAutoTargetJvm()
    }

    val release = platforms[name]?.second ?: 8
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(release)
        options.compilerArgs.add("-Xlint:-options")
    }

    tasks.withType<ProcessResources>().configureEach {
        val props = mapOf("version" to project.version.toString())
        inputs.properties(props)
        filesMatching(listOf("plugin.yml", "fabric.mod.json", "META-INF/mods.toml")) { expand(props) }
    }

    platforms[name]?.let { (label, _) ->
        // Bundle the shared modules into the platform jar (they are tiny and have no third-party dependencies).
        val bundled = configurations.named("runtimeClasspath")
        val archives = serviceOf<ArchiveOperations>()
        tasks.named<Jar>("jar") {
            archiveBaseName.set("RestStatsAPI")
            archiveClassifier.set(label)
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            dependsOn(bundled)
            from(bundled.map { cp: Configuration -> cp.filter { f: File -> f.name.endsWith(".jar") }.map { f: File -> archives.zipTree(f) } })
        }
    }
}

tasks.register<Copy>("releaseJars") {
    description = "Builds every platform jar into build/release"
    into(layout.buildDirectory.dir("release"))
    platforms.keys.forEach { from(project(":$it").tasks.named("jar")) }
}
