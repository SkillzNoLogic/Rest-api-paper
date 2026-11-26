dependencies {
    implementation(project(":vanilla"))
    compileOnly("net.minecraftforge:javafmllanguage:1.20.1-47.4.29") { isTransitive = false }
    compileOnly("org.slf4j:slf4j-api:2.0.9") // shipped with Minecraft
}
