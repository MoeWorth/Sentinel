package com.moeworth.sentinel.bukkit;

import com.moeworth.sentinel.bukkit.detection.NoopNetworkThreatService;
import com.moeworth.sentinel.bukkit.handshake.BackendHandshakeClient;
import com.moeworth.sentinel.bukkit.listener.PlayerJoinAnalysisListener;
import com.moeworth.sentinel.common.config.SentinelConfig;
import com.moeworth.sentinel.core.SentinelCore;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Point d'entree du plugin Bukkit (compatible Paper, Spigot, Bukkit).
 * <p>
 * Si Sentinel est deja actif sur le proxy BungeeCord en amont (poignee de
 * main confirmee), ce plugin se desactive avec un avertissement explicite
 * en console afin d'eviter une double analyse du meme joueur.
 */
public final class SentinelBukkitPlugin extends JavaPlugin {

    private SentinelCore core;

    @Override
    public void onEnable() {
        try {
            Path dataFolder = getDataFolder().toPath();
            Files.createDirectories(dataFolder);

            SentinelConfig config = loadOrCreateConfig(dataFolder);

            // TODO(implementation-plateforme) : remplacer par de vraies implementations,
            // voir sentinel-api.provider.* (identique a sentinel-bungee).
            this.core = SentinelCore.bootstrap(
                    config,
                    dataFolder,
                    new NoopNetworkThreatService(),
                    List.of()
            );

            BackendHandshakeClient handshakeClient = new BackendHandshakeClient(this, this::disableAfterProxyDetected);
            handshakeClient.register();

            getServer().getPluginManager().registerEvents(
                    new PlayerJoinAnalysisListener(this, core, handshakeClient), this);

            getLogger().info("Sentinel actif (mode serveur local).");
        } catch (Exception e) {
            getLogger().severe("Echec du demarrage de Sentinel : " + e.getMessage());
        }
    }

    /**
     * Appele lorsque la poignee de main confirme que le proxy fait deja
     * tourner Sentinel : desactive ce module backend pour eviter une
     * double analyse, avec un avertissement clair en console.
     */
    private void disableAfterProxyDetected() {
        getLogger().warning("Sentinel est deja actif sur le proxy (BungeeCord) : "
                + "desactivation de l'instance locale sur ce serveur pour eviter une analyse en double.");
        getServer().getPluginManager().disablePlugin(this);
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
            try (InputStream in = getResource("config.yml")) {
                Files.copy(in, configFile);
            }
        }
        return SentinelConfig.loadFromFile(configFile);
    }

    public SentinelCore core() {
        return core;
    }
}
