package com.moeworth.sentinel.api.model;

import java.util.Objects;

/**
 * Resultat immuable produit par un {@link com.moeworth.sentinel.api.check.TrustCheck}.
 *
 * @param checkId      identifiant technique unique de la verification (ex: "vpn-detection")
 * @param displayName  libelle humain affiche dans le rapport (ex: "Detection VPN/Proxy")
 * @param status       statut obtenu (succes, avertissement, echec, indetermine)
 * @param scoreImpact  impact applique au score final, positif ou negatif, deja pondere
 * @param reason       raison courte expliquant le resultat (affichee dans le rapport)
 */
public record CheckResult(
        String checkId,
        String displayName,
        CheckStatus status,
        double scoreImpact,
        String reason
) {
    public CheckResult {
        Objects.requireNonNull(checkId, "checkId ne peut pas etre nul");
        Objects.requireNonNull(displayName, "displayName ne peut pas etre nul");
        Objects.requireNonNull(status, "status ne peut pas etre nul");
        Objects.requireNonNull(reason, "reason ne peut pas etre nul");
    }
}
