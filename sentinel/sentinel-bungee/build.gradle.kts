/*
 * sentinel-bungee
 * Implementation de la plateforme BungeeCord. C'est ici, et uniquement
 * ici, que vivent les dependances vers l'API BungeeCord.
 */
plugins {
    id("com.gradleup.shadow") version "8.3.5"
}

dependencies {
    implementation(project(":sentinel-api"))
    implementation(project(":sentinel-common"))
    implementation(project(":sentinel-core"))
    compileOnly("net.md-5:bungeecord-api:1.21-R0.4")
}

repositories {
    maven { url = uri("https://libraries.minecraft.net") }
}

tasks.processResources {
    filesMatching("bungee.yml") {
        expand("version" to project.version.toString())
    }
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveBaseName.set("sentinel-bungee")
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}