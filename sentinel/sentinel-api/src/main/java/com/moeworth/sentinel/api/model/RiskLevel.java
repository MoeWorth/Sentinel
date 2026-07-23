package com.moeworth.sentinel.api.model;

/**
 * Niveau de risque global attribue a un joueur a l'issue de l'analyse.
 * Les seuils numeriques associes a chaque niveau sont definis en YAML
 * (voir sentinel-core/resources/config.yml), jamais en dur dans le code.
 */
public enum RiskLevel {
    FAIBLE,
    MOYEN,
    ELEVE,
    CRITIQUE
}
