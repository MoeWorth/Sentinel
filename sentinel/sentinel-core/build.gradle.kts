/*
 * sentinel-core
 * Coeur du systeme : moteur d'analyse, systeme de scoring, verifications
 * par defaut et stockage. NE DEPEND D'AUCUNE PLATEFORME (pas de Bukkit,
 * pas de BungeeCord API ici). Les modules de plateforme dependent de ce
 * module, jamais l'inverse.
 */
dependencies {
    api(project(":sentinel-api"))
    implementation(project(":sentinel-common"))
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
    implementation("org.slf4j:slf4j-api:2.0.16")
}
