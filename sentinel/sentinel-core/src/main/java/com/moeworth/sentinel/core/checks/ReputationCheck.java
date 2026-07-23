package com.moeworth.sentinel.core.checks;

import com.moeworth.sentinel.api.check.CheckContext;
import com.moeworth.sentinel.api.check.TrustCheck;
import com.moeworth.sentinel.api.model.CheckResult;
import com.moeworth.sentinel.api.model.CheckStatus;
import com.moeworth.sentinel.api.provider.ReputationProvider;

import java.util.List;

/**
 * Interroge une liste de {@link ReputationProvider} configures (listes noires
 * publiques, bases communautaires, ...) et agrege le pire signal rencontre.
 * L'ajout d'un nouveau fournisseur ne necessite aucune modification de
 * cette classe : il suffit de l'ajouter a la liste injectee.
 */
public final class ReputationCheck implements TrustCheck {

    private final List<ReputationProvider> providers;

    public ReputationCheck(List<ReputationProvider> providers) {
        this.providers = providers;
    }

    @Override
    public String id() {
        return "reputation";
    }

    @Override
    public String displayName() {
        return "Reputation externe";
    }

    @Override
    public CheckResult evaluate(CheckContext context) {
        if (providers.isEmpty()) {
            return new CheckResult(id(), displayName(), CheckStatus.INDETERMINE, 0.0,
                    "Aucun fournisseur de reputation configure");
        }

        double worstScore = 1.0;
        String worstDetail = null;
        boolean anyFlagged = false;
        boolean allFailed = true;

        for (ReputationProvider provider : providers) {
            try {
                var result = provider.lookup(context.playerUuid(), context.address());
                allFailed = false;
                if (result.flagged()) {
                    anyFlagged = true;
                }
                if (result.score() < worstScore) {
                    worstScore = result.score();
                    worstDetail = provider.id() + " : " + result.details();
                }
            } catch (ReputationProvider.ReputationLookupException ignored) {
                // Un fournisseur indisponible ne doit pas bloquer les autres.
            }
        }

        if (allFailed) {
            return new CheckResult(id(), displayName(), CheckStatus.INDETERMINE, 0.0,
                    "Tous les fournisseurs de reputation sont indisponibles");
        }
        if (anyFlagged) {
            return new CheckResult(id(), displayName(), CheckStatus.ECHEC, -5.0,
                    "Signale par un fournisseur de reputation : " + worstDetail);
        }
        if (worstScore < 0.7) {
            return new CheckResult(id(), displayName(), CheckStatus.AVERTISSEMENT, -1.5,
                    "Reputation moyenne : " + worstDetail);
        }
        return new CheckResult(id(), displayName(), CheckStatus.SUCCES, 0.0,
                "Aucun signalement trouve");
    }
}
