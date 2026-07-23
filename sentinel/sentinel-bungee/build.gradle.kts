/*
 * sentinel-bungee
 * Implementation de la plateforme BungeeCord. C'est ici, et uniquement
 * ici, que vivent les dependances vers l'API BungeeCord.
 */
dependencies {
    implementation(project(":sentinel-api"))
    implementation(project(":sentinel-common"))
    implementation(project(":sentinel-core"))
    // Publie directement sur Maven Central depuis la version 1.21-R0.4 :
    // plus besoin du depot snapshot oss.sonatype.org historique.
    compileOnly("net.md-5:bungeecord-api:1.21-R0.4")
}

repositories {
    // Requis pour com.mojang:brigadier, dependance transitive de bungeecord-api,
    // absente de Maven Central.
    maven { url = uri("https://libraries.minecraft.net") }
}

tasks.jar {
    // A terme : shadowJar pour embarquer sqlite-jdbc/snakeyaml/slf4j dans le plugin final.
    archiveBaseName.set("sentinel-bungee")
}
