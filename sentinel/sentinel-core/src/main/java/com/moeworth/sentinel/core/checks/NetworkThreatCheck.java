package com.moeworth.sentinel.core.checks;

import com.moeworth.sentinel.api.check.CheckContext;
import com.moeworth.sentinel.api.check.TrustCheck;
import com.moeworth.sentinel.api.model.CheckResult;
import com.moeworth.sentinel.api.model.CheckStatus;
import com.moeworth.sentinel.api.provider.NetworkThreatService;
import com.moeworth.sentinel.common.cache.SimpleCache;

import java.net.InetAddress;

/**
 * Verification VPN / Proxy / Tor.
 * Delegue l'analyse reseau a une implementation remplaçable de
 * {@link NetworkThreatService} (ex: appel a un service externe), avec mise
 * en cache des resultats pour eviter les appels repetes sur une meme IP.
 */
public final class NetworkThreatCheck implements TrustCheck {

    private final NetworkThreatService service;
    private final SimpleCache<InetAddress, NetworkThreatService.NetworkThreatResult> cache;

    public NetworkThreatCheck(NetworkThreatService service,
                               SimpleCache<InetAddress, NetworkThreatService.NetworkThreatResult> cache) {
        this.service = service;
        this.cache = cache;
    }

    @Override
    public String id() {
        return "network-threat";
    }

    @Override
    public String displayName() {
        return "Detection VPN / Proxy / Tor";
    }

    @Override
    public CheckResult evaluate(CheckContext context) {
        try {
            var result = cache.getOrCompute(context.address(), this::analyzeUnchecked);
            if (result == null) {
                return new CheckResult(id(), displayName(), CheckStatus.INDETERMINE, 0.0,
                        "Service d'analyse reseau indisponible");
            }
            if (!result.suspect()) {
                return new CheckResult(id(), displayName(), CheckStatus.SUCCES, 0.0,
                        "Aucune IP suspecte detectee");
            }
            String detail = describe(result);
            return new CheckResult(id(), displayName(), CheckStatus.ECHEC, -4.0, detail);
        } catch (RuntimeException e) {
            return new CheckResult(id(), displayName(), CheckStatus.INDETERMINE, 0.0,
                    "Erreur lors de l'analyse reseau : " + e.getMessage());
        }
    }

    private NetworkThreatService.NetworkThreatResult analyzeUnchecked(InetAddress address) {
        try {
            return service.analyze(address);
        } catch (NetworkThreatService.NetworkThreatLookupException e) {
            return null;
        }
    }

    private static String describe(NetworkThreatService.NetworkThreatResult result) {
        StringBuilder sb = new StringBuilder("IP suspecte : ");
        if (result.vpn()) sb.append("VPN ");
        if (result.proxy()) sb.append("Proxy ");
        if (result.tor()) sb.append("Tor ");
        if (result.hosting()) sb.append("Hebergeur/Datacenter ");
        return sb.toString().trim();
    }
}
