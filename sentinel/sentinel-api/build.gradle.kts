/*
 * sentinel-api
 * Module contenant UNIQUEMENT des interfaces, enums et modeles de donnees.
 * Aucune dependance de plateforme (Bukkit/Bungee/Velocity/...) ni de
 * dependance d'implementation (SQLite, SnakeYAML, ...) ne doit apparaitre ici.
 * C'est le contrat public que sentinel-core implemente et que les modules
 * de plateforme consomment.
 */
dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
}
