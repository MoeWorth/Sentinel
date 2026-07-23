package com.moeworth.sentinel.api.provider;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Abstraction d'un fournisseur de reputation (listes noires publiques,
 * bases communautaires de triche, blacklists de serveurs partenaires...).
 * <p>
 * Aucune dependance vers une API tierce specifique ne doit apparaitre dans
 * sentinel-api ou sentinel-core : chaque fournisseur concret (ex: appel HTTP
 * vers un service donne) est implemente dans un module a part et enregistre
 * dynamiquement via la configuration.
 */
public interface ReputationProvider {

    /** Identifiant unique du fournisseur, utilise dans la configuration YAML. */
    String id();

    /**
     * Interroge le fournisseur pour le joueur/IP donne.
     *
     * @param playerUuid UUID du joueur (peut etre ignore par certains fournisseurs)
     * @param address    adresse IP du joueur
     * @return un score de reputation entre 0.0 (tres mauvaise reputation) et 1.0 (aucun signalement)
     * @throws ReputationLookupException si l'appel au fournisseur echoue (timeout, erreur HTTP, ...)
     */
    ReputationResult lookup(UUID playerUuid, InetAddress address) throws ReputationLookupException;

    /**
     * Resultat d'une consultation de reputation.
     *
     * @param score       valeur normalisee entre 0.0 (mauvaise reputation) et 1.0 (bonne reputation)
     * @param flagged     true si le fournisseur signale explicitement ce joueur/IP
     * @param details     details textuels optionnels (raison du signalement, categorie, ...)
     */
    record ReputationResult(double score, boolean flagged, String details) {
    }

    /** Exception levee lorsqu'un fournisseur ne peut pas repondre. */
    class ReputationLookupException extends Exception {
        public ReputationLookupException(String message, Throwable cause) {
            super(message, cause);
        }

        public ReputationLookupException(String message) {
            super(message);
        }
    }
}
