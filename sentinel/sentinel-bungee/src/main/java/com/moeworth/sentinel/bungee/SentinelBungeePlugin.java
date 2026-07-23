package com.moeworth.sentinel.bungee;

import com.moeworth.sentinel.bungee.command.SentinelCommand;
import com.moeworth.sentinel.bungee.listener.PlayerConnectionListener;
import com.moeworth.sentinel.common.config.SentinelConfig;
import com.moeworth.sentinel.core.SentinelCore;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Point d'entree du plugin BungeeCord.
 * <p>
 * Responsabilite unique de cette classe : cablage (chargement de la
 * configuration, instanciation du coeur, enregistrement des listeners et
 * commandes specifiques a Bungee). Toute la logique metier vit dans
 * sentinel-core.
 */
public final class SentinelBungeePlugin extends Plugin {

    private SentinelCore core;

    @Override
    public void onEnable() {
        try {
            Path dataFolder = getDataFolder().toPath();
            Files.createDirectories(dataFolder);

            SentinelConfig config = loadOrCreateConfig(dataFolder);

            // TODO(implementation-plateforme) : remplacer par de vraies implementations
            // (service HTTP de detection VPN, fournisseurs de reputation configures)
            // une fois les integrations externes choisies. Voir sentinel-api.provider.*
            this.core = SentinelCore.bootstrap(
                    config,
                    dataFolder,
                    new com.moeworth.sentinel.bungee.detection.NoopNetworkThreatService(),
                    java.util.List.of()
            );

            getProxy().getPluginManager().registerListener(this, new PlayerConnectionListener(this, core));
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
