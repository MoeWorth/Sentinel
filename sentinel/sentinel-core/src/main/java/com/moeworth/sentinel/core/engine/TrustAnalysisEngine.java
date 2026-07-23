package com.moeworth.sentinel.core.engine;

import com.moeworth.sentinel.api.check.CheckContext;
import com.moeworth.sentinel.api.check.TrustCheck;
import com.moeworth.sentinel.api.model.CheckResult;
import com.moeworth.sentinel.api.model.CheckStatus;
import com.moeworth.sentinel.api.model.TrustReport;
import com.moeworth.sentinel.common.config.SentinelConfig;
import com.moeworth.sentinel.common.config.WeightConfig;
import com.moeworth.sentinel.common.util.SentinelLogger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Moteur central d'analyse de confiance.
 * <p>
 * Independant de toute plateforme : recoit un {@link CheckContext} deja
 * construit par le module de plateforme (Bungee, Velocity, ...), execute
 * l'ensemble des {@link TrustCheck} enregistrees, applique la ponderation
 * configuree, puis delegue le calcul du score a {@link ScoringService}.
 * <p>
 * Pour ajouter une nouvelle verification : implementer {@link TrustCheck}
 * et l'enregistrer via {@link #registerCheck(TrustCheck)}. Aucune
 * modification de cette classe n'est necessaire.
 */
public final class TrustAnalysisEngine {

    private static final SentinelLogger LOGGER = SentinelLogger.of(TrustAnalysisEngine.class);

    private final List<TrustCheck> checks = new CopyOnWriteArrayList<>();
    private final SentinelConfig config;
    private final ScoringService scoringService;

    public TrustAnalysisEngine(SentinelConfig config, ScoringService scoringService) {
        this.config = config;
        this.scoringService = scoringService;
    }

    /** Enregistre une nouvelle verification aupres du moteur. */
    public void registerCheck(TrustCheck check) {
        checks.add(check);
        LOGGER.debug("Verification enregistree : {}", check.id());
    }

    /**
     * Execute l'analyse complete pour un contexte de connexion donne et
     * produit le rapport final. Chaque verification est isolee : une
     * exception dans l'une d'elles ne bloque jamais les autres.
     */
    public TrustReport analyze(CheckContext context) {
        List<CheckResult> results = new ArrayList<>();

        for (TrustCheck check : checks) {
            WeightConfig weightConfig = config.checkWeight(check.id());
            if (!weightConfig.enabled()) {
                continue;
            }
            results.add(runSafely(check, context, weightConfig));
        }

        double score = scoringService.computeGlobalScore(results);

        return new TrustReport(
                context.playerUuid(),
                context.playerName(),
                score,
                scoringService.computeRiskLevel(score),
                results,
                Instant.now()
        );
    }

    private CheckResult runSafely(TrustCheck check, CheckContext context, WeightConfig weightConfig) {
        try {
            CheckResult raw = check.evaluate(context);
            double weightedImpact = raw.scoreImpact() * weightConfig.weight();
            return new CheckResult(raw.checkId(), raw.displayName(), raw.status(), weightedImpact, raw.reason());
        } catch (Exception e) {
            LOGGER.error("Echec de la verification '" + check.id() + "'", e);
            return new CheckResult(
                    check.id(),
                    check.displayName(),
                    CheckStatus.INDETERMINE,
                    0.0,
                    "Erreur interne lors de l'execution de la verification"
            );
        }
    }
}
