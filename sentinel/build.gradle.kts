/*
 * Build racine du projet Sentinel.
 * Ce fichier ne contient AUCUNE dependance de plateforme : il ne fait que
 * mutualiser la configuration commune (Java, encodage, depots) a tous
 * les sous-modules. Chaque module declare ses propres dependances.
 */
plugins {
    java
}

allprojects {
    group = "com.moeworth.sentinel"
    version = project.property("sentinelVersion") as String

    repositories {
        mavenCentral()
    }
}

subprojects {
    // java-library (et non simplement "java") est necessaire pour pouvoir
    // utiliser la configuration api(...) dans les build.gradle.kts des
    // sous-modules (ex: sentinel-common et sentinel-core exposent
    // sentinel-api en transitif via api(project(":sentinel-api"))).
    apply(plugin = "java-library")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
