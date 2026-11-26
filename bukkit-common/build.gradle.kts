dependencies {
    api(project(":core"))
    // Oldest API we support, so the shared code only uses methods that exist on every Bukkit version.
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
}
