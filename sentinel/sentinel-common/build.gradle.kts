/*
 * sentinel-common
 * Utilitaires partages (chargement de configuration YAML, logging, cache)
 * independants de toute plateforme de jeu.
 */
dependencies {
    api(project(":sentinel-api"))
    implementation("org.yaml:snakeyaml:2.2")
    implementation("org.slf4j:slf4j-api:2.0.16")
}
