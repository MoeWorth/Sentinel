rootProject.name = "sentinel"

include(
    "sentinel-api",
    "sentinel-common",
    "sentinel-core",
    "sentinel-bungee",
    // Un seul module Bukkit couvre Paper, Spigot et Bukkit : les trois
    // implementent la meme API Bukkit, donc un seul jar compile suffit.
    // Il est ensuite duplique/renomme en 3 artefacts pour Modrinth (voir
    // sentinel-bukkit/build.gradle.kts).
    "sentinel-bukkit",
    "sentinel-velocity"
    // Modules futurs, a decommenter quand implementes :
    // "sentinel-fabric",
    // "sentinel-forge"
)
