package com.moeworth.sentinel.core.engine;

import com.moeworth.sentinel.api.model.CheckResult;
import com.moeworth.sentinel.api.model.RiskLevel;
import com.moeworth.sentinel.common.config.SentinelConfig;

import java.util.List;

/**
 * Calcule le score global (0-10) et le niveau de risque a partir des
 * resultats individuels de verifications, en appliquant les seuils
 * definis dans la configuration YAML.
 * <p>
 * Le score de depart est 10.0 (confiance maximale) ; chaque {@link CheckResult}
 * applique son {@code scoreImpact} (deja pondere en amont par le moteur),
 * puis le total est borne entre 0 et 10.
 */
public final class ScoringService {

    private static final double SCORE_MAX = 10.0;
    private static final double SCORE_MIN = 0.0;

    private final SentinelConfig config;

    public ScoringService(SentinelConfig config) {
        this.config = config;
    }

    /** Calcule le score global a partir de la liste de resultats. */
    public double computeGlobalScore(List<CheckResult> results) {
        double score = SCORE_MAX;
        for (CheckResult result : results) {
            score += result.scoreImpact();
        }
        return Math.max(SCORE_MIN, Math.min(SCORE_MAX, score));
    }

    /**
     * Determine le niveau de risque correspondant au score, selon les seuils
     * configures (thresholds.moyen, thresholds.eleve, thresholds.critique).
     * Par defaut : >=7.5 Faible, >=5 Moyen, >=2.5 Eleve, sinon Critique.
     */
    public RiskLevel computeRiskLevel(double score) {
        double moyenThreshold = config.threshold("moyen", 7.5);
        double eleveThreshold = config.threshold("eleve", 5.0);
        double critiqueThreshold = config.threshold("critique", 2.5);

        if (score >= moyenThreshold) {
            return RiskLevel.FAIBLE;
        } else if (score >= eleveThreshold) {
            return RiskLevel.MOYEN;
        } else if (score >= critiqueThreshold) {
            return RiskLevel.ELEVE;
        } else {
            return RiskLevel.CRITIQUE;
        }
    }
}
