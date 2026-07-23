package com.moeworth.sentinel.api.provider;

import java.net.InetAddress;

/**
 * Abstraction d'un service de detection VPN / Proxy / Tor pour une adresse IP.
 * L'implementation concrete (appel a un service externe remplaçable) vit en
 * dehors de sentinel-api et sentinel-core, et est injectee via la configuration.
 */
public interface NetworkThreatService {

    /** Identifiant unique du service, utilise dans la configuration YAML. */
    String id();

    /**
     * Analyse l'adresse IP fournie.
     *
     * @param address adresse a analyser
     * @return le resultat de l'analyse reseau
     * @throws NetworkThreatLookupException si le service est indisponible
     */
    NetworkThreatResult analyze(InetAddress address) throws NetworkThreatLookupException;

    /**
     * @param vpn      true si l'IP est identifiee comme VPN
     * @param proxy    true si l'IP est identifiee comme proxy ouvert
     * @param tor      true si l'IP est identifiee comme noeud de sortie Tor
     * @param hosting  true si l'IP appartient a un hebergeur/datacenter (souvent correle a un VPN)
     */
    record NetworkThreatResult(boolean vpn, boolean proxy, boolean tor, boolean hosting) {
        public boolean suspect() {
            return vpn || proxy || tor || hosting;
        }
    }

    /** Exception levee lorsque le service d'analyse reseau ne peut pas repondre. */
    class NetworkThreatLookupException extends Exception {
        public NetworkThreatLookupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
