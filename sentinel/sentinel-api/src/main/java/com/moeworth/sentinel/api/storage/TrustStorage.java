package com.moeworth.sentinel.api.storage;

import com.moeworth.sentinel.api.model.TrustReport;

import java.util.Optional;
import java.util.UUID;

/**
 * Abstraction de persistance des rapports de confiance.
 * L'implementation par defaut (sentinel-core) utilise SQLite ; une
 * implementation PostgreSQL optionnelle peut etre fournie sans changer
 * ce contrat.
 */
public interface TrustStorage {

    /** Initialise le support de stockage (creation de schema si necessaire). */
    void initialize() throws StorageException;

    /** Sauvegarde (ou met a jour) le rapport le plus recent pour un joueur. */
    void save(TrustReport report) throws StorageException;

    /** Recupere le dernier rapport connu pour un joueur, si existant. */
    Optional<TrustReport> findLatest(UUID playerUuid) throws StorageException;

    /** Ferme proprement les ressources (connexions, pool, ...). */
    void close();

    /** Exception generique de couche de stockage. */
    class StorageException extends Exception {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }

        public StorageException(String message) {
            super(message);
        }
    }
}
