/*
 * sentinel-velocity
 * Implementation de la plateforme Velocity. Comme sentinel-bungee, ce
 * module contient uniquement le cablage specifique au proxy ; toute la
 * logique metier reste dans sentinel-core.
 * <p>
 * Contrairement a BungeeCord, Velocity ne necessite aucun fichier de
 * manifeste separe : les metadonnees du plugin (id, nom, version, auteurs)
 * sont declarees via l'annotation @Plugin sur la classe principale et
 * generees automatiquement dans le jar par l'annotation processor
 * (velocity-api) a la compilation.
 */
plugins {
    id("com.gradleup.shadow") version "8.3.5"
}

dependencies {
    implementation(project(":sentinel-api"))
    implementation(project(":sentinel-common"))
    implementation(project(":sentinel-core"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0")
}

repositories {
    // Velocity, comme Paper, est publie par PaperMC sur son propre depot.
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveFileName.set("sentinel-${project.version}+velocity.jar")
    mergeServiceFiles()
    isZip64 = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
