/*
 * sentinel-bukkit
 * Implementation commune pour Paper, Spigot et Bukkit : les trois exposent
 * la meme API Bukkit (Paper l'etend juste avec des ajouts optionnels non
 * utilises ici). Un seul jar est donc compile, puis duplique/renomme en
 * 3 artefacts distincts pour Modrinth (un par loader), sans dupliquer de code.
 */
plugins {
    id("com.gradleup.shadow") version "8.3.5"
}

dependencies {
    implementation(project(":sentinel-api"))
    implementation(project(":sentinel-common"))
    implementation(project(":sentinel-core"))
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version.toString())
    }
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveFileName.set("sentinel-${project.version}+bukkit.jar")
    mergeServiceFiles()
    isZip64 = false
}

val copyAsPaperJar by tasks.registering(Copy::class) {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.flatMap { it.archiveFile })
    into(layout.buildDirectory.dir("libs"))
    rename { "sentinel-${project.version}+paper.jar" }
}

val copyAsSpigotJar by tasks.registering(Copy::class) {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.flatMap { it.archiveFile })
    into(layout.buildDirectory.dir("libs"))
    rename { "sentinel-${project.version}+spigot.jar" }
    // Les deux taches ecrivent dans le meme dossier (build/libs) sans se
    // lire l'une l'autre : Gradle ne peut pas deviner qu'elles n'entrent
    // pas en conflit et refuse de les paralleliser sans ordre explicite.
    mustRunAfter(copyAsPaperJar)
}

tasks.build {
    dependsOn(tasks.shadowJar, copyAsPaperJar, copyAsSpigotJar)
}