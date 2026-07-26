package com.moeworth.sentinel.velocity.handshake;

import com.moeworth.sentinel.common.handshake.ProxyHandshakeProtocol;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;

import java.util.Arrays;

/**
 * Repond aux messages de poignee de main envoyes par les modules backend
 * (Paper/Spigot/Bukkit/Fabric/Forge) afin de leur signaler que Sentinel est
 * deja actif sur le proxy Velocity : ceux-ci se desactivent alors pour
 * eviter une double analyse du meme joueur (voir {@link ProxyHandshakeProtocol}).
 */
public final class ProxyHandshakeListener {

    private final ChannelIdentifier channel;

    public ProxyHandshakeListener(ChannelIdentifier channel) {
        this.channel = channel;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(channel)) {
            return;
        }
        if (!(event.getSource() instanceof ServerConnection serverConnection)) {
            return;
        }
        if (!Arrays.equals(ProxyHandshakeProtocol.PING, event.getData())) {
            return;
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        serverConnection.sendPluginMessage(channel, ProxyHandshakeProtocol.PONG);
    }
}
