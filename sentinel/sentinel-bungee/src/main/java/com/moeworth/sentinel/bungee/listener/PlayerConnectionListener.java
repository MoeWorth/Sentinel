package com.moeworth.sentinel.bungee.listener;

import com.moeworth.sentinel.api.check.CheckContext;
import com.moeworth.sentinel.api.model.TrustReport;
import com.moeworth.sentinel.bungee.SentinelBungeePlugin;
import com.moeworth.sentinel.core.SentinelCore;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.CompletableFuture;

/**
 * Ecoute la premiere connexion d'un joueur au proxy et declenche l'analyse
 * de confiance de maniere asynchrone (afin de ne jamais bloquer le thread
 * reseau de BungeeCord).
 */
public final class PlayerConnectionListener implements Listener {

    private final SentinelBungeePlugin plugin;
    private final SentinelCore core;

    public PlayerConnectionListener(SentinelBungeePlugin plugin, SentinelCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // NOTE : le Client Brand (minecraft:brand) n'est disponible qu'apres
        // l'entree en jeu du joueur sur un serveur backend, pas au moment du
        // PostLogin sur le proxy. Une implementation complete devra ecouter
        // le canal de plugin correspondant et mettre a jour le contexte /
        // relancer partiellement l'analyse. Cette version fournit la
        // structure ; le raccordement fin est laisse en TODO.
        CheckContext context = new CheckContext(
                player.getUniqueId(),
                player.getName(),
                player.getSocketAddress() instanceof java.net.InetSocketAddress isa ? isa.getAddress() : null,
                player.getPendingConnection().isOnlineMode(),
                player.getPendingConnection().getVersion(),
                null,
                null
        );

        CompletableFuture.supplyAsync(() -> core.engine().analyze(context))
                .thenAccept(this::handleReport)
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Erreur durant l'analyse de confiance : " + ex.getMessage());
                    return null;
                });
    }

    private void handleReport(TrustReport report) {
        try {
            core.storage().save(report);
        } catch (Exception e) {
            plugin.getLogger().severe("Impossible d'enregistrer le rapport de confiance : " + e.getMessage());
        }

        // TODO(implementation-plateforme) : appliquer l'action configuree
        // (autoriser / avertir / refuser) selon report.riskLevel() et la
        // section "actions" de config.yml, ex: event.getPlayer().disconnect(...).
        plugin.getLogger().info("Rapport de confiance pour " + report.playerName()
                + " : score=" + report.globalScore() + " risque=" + report.riskLevel());
    }
}
