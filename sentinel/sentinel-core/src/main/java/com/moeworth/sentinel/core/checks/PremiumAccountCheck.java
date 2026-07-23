package com.moeworth.sentinel.core.checks;

import com.moeworth.sentinel.api.check.CheckContext;
import com.moeworth.sentinel.api.check.TrustCheck;
import com.moeworth.sentinel.api.model.CheckResult;
import com.moeworth.sentinel.api.model.CheckStatus;

/**
 * Verifie si le compte est Premium (authentifie Mojang) ou en mode hors ligne.
 * La valeur {@link CheckContext#isPremium()} doit etre renseignee en amont
 * par le module de plateforme (ex: resultat du handshake Bungee "online-mode").
 */
public final class PremiumAccountCheck implements TrustCheck {

    @Override
    public String id() {
        return "premium-account";
    }

    @Override
    public String displayName() {
        return "Compte Premium";
    }

    @Override
    public CheckResult evaluate(CheckContext context) {
        Boolean premium = context.isPremium();
        if (premium == null) {
            return new CheckResult(id(), displayName(), CheckStatus.INDETERMINE, 0.0,
                    "Statut premium non verifiable sur cette configuration de serveur");
        }
        if (premium) {
            return new CheckResult(id(), displayName(), CheckStatus.SUCCES, 0.0,
                    "Compte authentifie via Mojang");
        }
        return new CheckResult(id(), displayName(), CheckStatus.AVERTISSEMENT, -2.0,
                "Connexion en mode hors ligne (compte non premium ou cracke)");
    }
}
