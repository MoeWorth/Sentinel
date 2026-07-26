package com.moeworth.sentinel.common.handshake;

import java.nio.charset.StandardCharsets;

/**
 * Protocole minimal de "poignee de main" entre un module backend (Paper,
 * Spigot, Bukkit) et le module proxy (BungeeCord), utilise pour eviter une
 * double analyse d'un meme joueur si Sentinel est deja actif sur le proxy.
 * <p>
 * Fonctionnement : au premier login d'un joueur, le module backend envoie
 * {@link #PING} sur le canal de plugin {@link #CHANNEL}. Si le proxy
 * repond {@link #PONG} sur ce meme canal, le module backend se desactive
 * (Sentinel est deja gere en amont, au niveau du proxy).
 * <p>
 * Regroupe ici, dans sentinel-common, pour eviter toute divergence de
 * constantes entre les modules de plateforme.
 */
public final class ProxyHandshakeProtocol {

    /** Canal de plugin utilise pour la poignee de main proxy <-> backend. */
    public static final String CHANNEL = "sentinel:main";

    public static final byte[] PING = "SENTINEL_PING".getBytes(StandardCharsets.UTF_8);
    public static final byte[] PONG = "SENTINEL_PONG".getBytes(StandardCharsets.UTF_8);

    private ProxyHandshakeProtocol() {
    }
}