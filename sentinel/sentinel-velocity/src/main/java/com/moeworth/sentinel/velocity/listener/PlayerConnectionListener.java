package com.moeworth.sentinel.velocity.listener;

import com.moeworth.sentinel.api.check.CheckContext;
import com.moeworth.sentinel.api.model.TrustReport;
import com.moeworth.sentinel.core.SentinelCore;
import com.moeworth.sentinel.velocity.SentinelVelocityPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Ecoute la premiere connexion d'un joueur au proxy Velocity et declenche
 * l'analyse de confiance de maniere asynchrone (equivalent Velocity du
 * PlayerConnectionListener de sentinel-bungee).
 */
public final class PlayerConnectionListener {

    private final SentinelVelocityPlugin plugin;
    private final SentinelCore core;

    public PlayerConnectionListener(SentinelVelocityPlugin plugin, SentinelCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        var player = event.getPlayer();

        // NOTE : comme pour Bungee, le Client Brand n'est disponible qu'une
        // fois le joueur bascule sur un serveur backend. Raccordement fin
        // laisse en TODO, cf. sentinel-bungee.
        CheckContext context = new CheckContext(
                player.getUniqueId(),
                player.getUsername(),
                player.getRemoteAddress() instanceof InetSocketAddress isa ? isa.getAddress() : null,
                player.isOnlineMode(),
                player.getProtocolVersion().getProtocol(),
                null,
                null
        );

        CompletableFuture.supplyAsync(() -> core.engine().analyze(context))
                .thenAccept(this::handleReport)
                .exceptionally(ex -> {
                    plugin.logger().error("Erreur durant l'analyse de confiance", ex);
                    return null;
                });
    }

    private void handleReport(TrustReport report) {
        try {
            core.storage().save(report);
        } catch (Exception e) {
            plugin.logger().error("Impossible d'enregistrer le rapport de confiance", e);
        }

        // TODO(implementation-plateforme) : appliquer l'action configuree
        // (autoriser/avertir/refuser) selon report.riskLevel(), comme sur Bungee.
        plugin.logger().info("Rapport de confiance pour " + report.playerName()
                + " : score=" + report.globalScore() + " risque=" + report.riskLevel());
    }
}
