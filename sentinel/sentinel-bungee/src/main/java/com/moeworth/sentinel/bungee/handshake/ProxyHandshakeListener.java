package com.moeworth.sentinel.bungee.handshake;

import com.moeworth.sentinel.common.handshake.ProxyHandshakeProtocol;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;

/**
 * Repond aux messages de poignee de main envoyes par les modules backend
 * (Paper/Spigot/Bukkit) afin de leur signaler que Sentinel est deja actif
 * sur le proxy : ceux-ci se desactivent alors pour eviter une double
 * analyse du meme joueur (voir {@link ProxyHandshakeProtocol}).
 */
public final class ProxyHandshakeListener implements Listener {

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!ProxyHandshakeProtocol.CHANNEL.equals(event.getTag())) {
            return;
        }
        // On ne repond qu'aux messages venant d'un serveur backend, jamais
        // d'un client (protection basique contre un faux message client).
        if (!(event.getSender() instanceof Server server)) {
            return;
        }
        if (!Arrays.equals(ProxyHandshakeProtocol.PING, event.getData())) {
            return;
        }
        event.setCancelled(true);
        server.sendData(ProxyHandshakeProtocol.CHANNEL, ProxyHandshakeProtocol.PONG);
    }
}