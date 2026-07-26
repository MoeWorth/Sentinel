package com.moeworth.sentinel.bukkit.detection;

import com.moeworth.sentinel.api.provider.NetworkThreatService;

import java.net.InetAddress;

/**
 * Implementation par defaut, neutre, identique a celle du module Bungee.
 * A remplacer par une vraie implementation une fois un service externe
 * de detection VPN/Proxy/Tor choisi.
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
