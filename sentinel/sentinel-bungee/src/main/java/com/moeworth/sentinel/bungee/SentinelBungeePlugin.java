package com.moeworth.sentinel.bungee;

import com.moeworth.sentinel.bungee.command.SentinelCommand;
import com.moeworth.sentinel.bungee.handshake.ProxyHandshakeListener;
import com.moeworth.sentinel.bungee.listener.PlayerConnectionListener;
import com.moeworth.sentinel.common.config.SentinelConfig;
import com.moeworth.sentinel.common.handshake.ProxyHandshakeProtocol;
import com.moeworth.sentinel.core.SentinelCore;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SentinelBungeePlugin extends Plugin {

    private SentinelCore core;

    @Override
    public void onEnable() {
        try {
            Path dataFolder = getDataFolder().toPath();
            Files.createDirectories(dataFolder);

            SentinelConfig config = loadOrCreateConfig(dataFolder);

            this.core = SentinelCore.bootstrap(
                    config,
                    dataFolder,
                    new com.moeworth.sentinel.bungee.detection.NoopNetworkThreatService(),
                    java.util.List.of()
            );

            getProxy().registerChannel(ProxyHandshakeProtocol.CHANNEL);
            getProxy().getPluginManager().registerListener(this, new PlayerConnectionListener(this, core));
            getProxy().getPluginManager().registerListener(this, new ProxyHandshakeListener());
            getProxy().getPluginManager().registerCommand(this, new SentinelCommand(core));

            getLogger().info("Sentinel active avec succes.");
        } catch (Exception e) {
            getLogger().severe("Echec du demarrage de Sentinel : " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (core != null) {
            core.shutdown();
        }
    }

    private SentinelConfig loadOrCreateConfig(Path dataFolder) throws IOException {
        Path configFile = dataFolder.resolve("config.yml");
        if (Files.notExists(configFile)) {
            try (var in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile);
            }
        }
        return SentinelConfig.loadFromFile(configFile);
    }

    public SentinelCore core() {
        return core;
    }
}