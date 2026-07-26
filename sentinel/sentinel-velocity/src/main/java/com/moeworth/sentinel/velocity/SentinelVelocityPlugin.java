package com.moeworth.sentinel.velocity;

import com.google.inject.Inject;
import com.moeworth.sentinel.common.config.SentinelConfig;
import com.moeworth.sentinel.common.handshake.ProxyHandshakeProtocol;
import com.moeworth.sentinel.core.SentinelCore;
import com.moeworth.sentinel.velocity.command.SentinelCommand;
import com.moeworth.sentinel.velocity.detection.NoopNetworkThreatService;
import com.moeworth.sentinel.velocity.handshake.ProxyHandshakeListener;
import com.moeworth.sentinel.velocity.listener.PlayerConnectionListener;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Point d'entree du plugin Velocity.
 * <p>
 * Meme role que sentinel-bungee.SentinelBungeePlugin, adapte au cycle de
 * vie et a l'injection de dependances (Guice) propres a Velocity. Toute la
 * logique metier reste dans sentinel-core.
 */
@Plugin(
        id = "sentinel",
        name = "Sentinel",
        version = "1.0.0-SNAPSHOT",
        description = "Systeme avance d'analyse de confiance des joueurs.",
        authors = {"Moeworth Studios"}
)
public final class SentinelVelocityPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private SentinelCore core;

    @Inject
    public SentinelVelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        try {
            Files.createDirectories(dataDirectory);
            SentinelConfig config = loadOrCreateConfig();

            // TODO(implementation-plateforme) : remplacer par de vraies implementations,
            // voir sentinel-api.provider.* (identique aux autres modules de plateforme).
            this.core = SentinelCore.bootstrap(config, dataDirectory, new NoopNetworkThreatService(), List.of());

            var channel = MinecraftChannelIdentifier.from(ProxyHandshakeProtocol.CHANNEL);
            server.getChannelRegistrar().register(channel);

            server.getEventManager().register(this, new PlayerConnectionListener(this, core));
            server.getEventManager().register(this, new ProxyHandshakeListener(channel));

            var commandMeta = server.getCommandManager().metaBuilder("sentinel").build();
            server.getCommandManager().register(commandMeta, new SentinelCommand(core, server));

            logger.info("Sentinel active avec succes.");
        } catch (Exception e) {
            logger.error("Echec du demarrage de Sentinel", e);
        }
    }

    private SentinelConfig loadOrCreateConfig() throws IOException {
        Path configFile = dataDirectory.resolve("config.yml");
        if (Files.notExists(configFile)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(in, configFile);
            }
        }
        return SentinelConfig.loadFromFile(configFile);
    }

    public SentinelCore core() {
        return core;
    }

    public Logger logger() {
        return logger;
    }
}