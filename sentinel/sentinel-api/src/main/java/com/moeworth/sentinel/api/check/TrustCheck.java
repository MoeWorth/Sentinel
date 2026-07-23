package com.moeworth.sentinel.api.check;

import com.moeworth.sentinel.api.model.CheckResult;

/**
 * Contrat commun a toute verification de confiance.
 * <p>
 * Pour ajouter une nouvelle verification (ex: "reputation Discord",
 * "empreinte TLS", ...), il suffit d'implementer cette interface et de
 * l'enregistrer aupres du moteur (voir sentinel-core), sans toucher au
 * coeur du systeme.
 */
public interface TrustCheck {

    /** Identifiant technique unique, utilise comme cle dans le fichier de configuration (poids, activation). */
    String id();

    /** Libelle humain affiche dans le rapport final. */
    String displayName();

    /**
     * Execute la verification pour le contexte donne.
     * L'implementation doit rester defensive : toute exception doit etre
     * capturee et traduite en {@link com.moeworth.sentinel.api.model.CheckStatus#INDETERMINE}.
     *
     * @param context informations disponibles sur la connexion
     * @return le resultat de la verification
     */
    CheckResult evaluate(CheckContext context);
}
