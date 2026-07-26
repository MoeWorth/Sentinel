package com.moeworth.sentinel.bukkit.handshake;

import com.moeworth.sentinel.common.handshake.ProxyHandshakeProtocol;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Envoie une poignee de main au proxy (BungeeCord) via le canal
 * {@link ProxyHandshakeProtocol#CHANNEL} au premier joueur connecte, et
 * declenche une action (desactivation du plugin) si le proxy repond,
 * signe que Sentinel y est deja actif.
 * <p>
 * Cf. note fonctionnelle : eviter une double analyse d'un meme joueur
 * lorsque le proxy et le serveur backend ont tous deux Sentinel installe.
 */
public final class BackendHandshakeClient implements PluginMessageListener {

    private final Plugin plugin;
    private final Runnable onProxyDetected;
    private final AtomicBoolean triggered = new AtomicBoolean(false);

    public BackendHandshakeClient(Plugin plugin, Runnable onProxyDetected) {
        this.plugin = plugin;
        this.onProxyDetected = onProxyDetected;
    }

    /** Enregistre les canaux entrant/sortant necessaires a la poignee de main. */
    public void register() {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, ProxyHandshakeProtocol.CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, ProxyHandshakeProtocol.CHANNEL, this);
    }

    /** Envoie le PING via la connexion du joueur donne (vehicule obligatoire de l'API Bukkit). */
    public void sendHandshake(Player player) {
        if (triggered.get()) {
            return;
        }
        player.sendPluginMessage(plugin, ProxyHandshakeProtocol.CHANNEL, ProxyHandshakeProtocol.PING);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!ProxyHandshakeProtocol.CHANNEL.equals(channel)) {
            return;
        }
        if (!Arrays.equals(ProxyHandshakeProtocol.PONG, message)) {
            return;
        }
        if (triggered.compareAndSet(false, true)) {
            onProxyDetected.run();
        }
    }
}
