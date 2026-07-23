package com.moeworth.sentinel.core.checks;

import com.moeworth.sentinel.api.check.CheckContext;
import com.moeworth.sentinel.api.check.TrustCheck;
import com.moeworth.sentinel.api.client.ClientType;
import com.moeworth.sentinel.api.model.CheckResult;
import com.moeworth.sentinel.api.model.CheckStatus;

import java.util.Set;

/**
 * Analyse le Client Brand (canal "minecraft:brand") et le type de client
 * detecte afin de signaler les clients modifies connus pour faciliter la
 * triche (ceci ne prejuge pas d'une triche reelle : simple signal parmi
 * d'autres, pondere en configuration).
 */
public final class ClientBrandCheck implements TrustCheck {

    private static final Set<ClientType> CLIENTS_A_SURVEILLER = Set.of(
            ClientType.LUNAR_CLIENT, ClientType.BADLION, ClientType.LABYMOD, ClientType.FEATHER
    );

    @Override
    public String id() {
        return "client-brand";
    }

    @Override
    public String displayName() {
        return "Type de client";
    }

    @Override
    public CheckResult evaluate(CheckContext context) {
        var clientInfo = context.clientInfo();
        if (clientInfo == null || clientInfo.type() == null) {
            return new CheckResult(id(), displayName(), CheckStatus.INDETERMINE, 0.0,
                    "Client non identifiable (aucune information transmise)");
        }

        ClientType type = clientInfo.type();
        if (type == ClientType.INCONNU) {
            return new CheckResult(id(), displayName(), CheckStatus.AVERTISSEMENT, -0.5,
                    "Client brand present mais non reconnu : " + clientInfo.brand());
        }
        if (CLIENTS_A_SURVEILLER.contains(type)) {
            return new CheckResult(id(), displayName(), CheckStatus.AVERTISSEMENT, -0.5,
                    "Client tiers detecte : " + type);
        }
        return new CheckResult(id(), displayName(), CheckStatus.SUCCES, 0.0,
                "Client standard detecte : " + type);
    }
}
