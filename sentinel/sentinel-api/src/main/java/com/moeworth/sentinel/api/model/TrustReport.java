package com.moeworth.sentinel.api.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Rapport final d'analyse de confiance pour un joueur.
 * Genere par le moteur d'analyse (sentinel-core) a partir de l'ensemble
 * des {@link CheckResult} produits par les verifications actives.
 *
 * @param playerUuid   UUID du joueur analyse
 * @param playerName   pseudo du joueur au moment de l'analyse
 * @param globalScore  score final, borne entre 0.0 et 10.0
 * @param riskLevel    niveau de risque derive du score (seuils configurables)
 * @param results      detail de chaque verification executee
 * @param generatedAt  horodatage de generation du rapport
 */
public record TrustReport(
        UUID playerUuid,
        String playerName,
        double globalScore,
        RiskLevel riskLevel,
        List<CheckResult> results,
        Instant generatedAt
) {
    /** Retourne uniquement les verifications en echec, utile pour l'affichage/logs. */
    public List<CheckResult> echecs() {
        return results.stream().filter(r -> r.status() == CheckStatus.ECHEC).toList();
    }

    /** Retourne uniquement les avertissements. */
    public List<CheckResult> avertissements() {
        return results.stream().filter(r -> r.status() == CheckStatus.AVERTISSEMENT).toList();
    }
}
