package com.moeworth.sentinel.api.model;

/**
 * Statut individuel du resultat d'une verification ({@link com.moeworth.sentinel.api.check.TrustCheck}).
 */
public enum CheckStatus {
    /** La verification s'est deroulee normalement et n'a rien signale d'anormal. */
    SUCCES,
    /** La verification a detecte un element suspect mais non bloquant. */
    AVERTISSEMENT,
    /** La verification a detecte un element considere comme un echec grave. */
    ECHEC,
    /** La verification n'a pas pu s'executer (service externe indisponible, timeout, etc.). */
    INDETERMINE
}
