package com.moeworth.sentinel.bungee.detection;

import com.moeworth.sentinel.api.provider.NetworkThreatService;

import java.net.InetAddress;

/**
 * Implementation par defaut, neutre, de {@link NetworkThreatService}.
 * Ne detecte jamais rien : elle sert de valeur par defaut "cle en main" tant
 * qu'aucun service externe de detection VPN/Proxy/Tor n'a ete branche.
 * <p>
 * Remplacer par une vraie implementation (appel HTTP vers un service tiers)
 * en l'injectant a la place de celle-ci dans {@code SentinelBungeePlugin}.
 */
public final class NoopNetworkThreatService implements NetworkThreatService {

    @Override
    public String id() {
        return "noop";
    }

    @Override
    public NetworkThreatResult analyze(InetAddress address) {
        return new NetworkThreatResult(false, false, false, false);
    }
}
