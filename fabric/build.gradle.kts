dependencies {
    implementation(project(":vanilla"))
    compileOnly("net.fabricmc:fabric-loader:0.15.11") { isTransitive = false }
    compileOnly("org.slf4j:slf4j-api:2.0.9") // shipped with Minecraft
}
