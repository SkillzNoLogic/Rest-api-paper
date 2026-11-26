plugins {
    id("xyz.jpenilla.run-paper")
}

dependencies {
    implementation(project(":bukkit-common"))
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}

tasks.runServer {
    minecraftVersion("1.21.11")
    runDirectory(rootProject.file("run"))
    jvmArgs("-Xms2G", "-Xmx2G")
}
