package com.moeworth.sentinel.bukkit.listener;

import com.moeworth.sentinel.api.check.CheckContext;
import com.moeworth.sentinel.api.model.TrustReport;
import com.moeworth.sentinel.bukkit.SentinelBukkitPlugin;
import com.moeworth.sentinel.bukkit.handshake.BackendHandshakeClient;
import com.moeworth.sentinel.core.SentinelCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetSocketAddress;

/**
 * Declenche l'analyse de confiance a la premiere connexion d'un joueur sur
 * ce serveur, et envoie en parallele la poignee de main proxy (voir
 * {@link BackendHandshakeClient}). Si le proxy repond, l'analyse locale
 * devient redondante ; le plugin se desactive alors (cf. onProxyDetected
 * dans {@link SentinelBukkitPlugin}).
 */
public final class PlayerJoinAnalysisListener implements Listener {

    private final SentinelBukkitPlugin plugin;
    private final SentinelCore core;
    private final BackendHandshakeClient handshakeClient;

    public PlayerJoinAnalysisListener(SentinelBukkitPlugin plugin, SentinelCore core,
                                       BackendHandshakeClient handshakeClient) {
        this.plugin = plugin;
        this.core = core;
        this.handshakeClient = handshakeClient;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        // Poignee de main envoyee systematiquement : si un proxy Sentinel
        // repond, la logique de desactivation prend le relais (voir
        // SentinelBukkitPlugin#onEnable).
        handshakeClient.sendHandshake(player);

        // NOTE : le protocolVersion precis n'est pas expose par l'API Bukkit
        // standard (necessite un lib de protocole comme ProtocolLib). Laisse
        // a 0 dans cette base ; a completer si une telle dependance est ajoutee.
        CheckContext context = new CheckContext(
                player.getUniqueId(),
                player.getName(),
                player.getAddress() instanceof InetSocketAddress isa ? isa.getAddress() : null,
                plugin.getServer().getOnlineMode(),
                0,
                null,
                null
        );

        // Executee un tick plus tard pour laisser le temps a une eventuelle
        // reponse de poignee de main d'arriver avant de lancer une analyse
        // locale qui pourrait devenir inutile.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    return; // Desactive entre-temps suite a la poignee de main.
                }
                TrustReport report = core.engine().analyze(context);
                try {
                    core.storage().save(report);
                } catch (Exception e) {
                    plugin.getLogger().severe("Impossible d'enregistrer le rapport de confiance : " + e.getMessage());
                }
                plugin.getLogger().info("Rapport de confiance pour " + report.playerName()
                        + " : score=" + report.globalScore() + " risque=" + report.riskLevel());
            }
        }.runTaskLater(plugin, 5L);
    }
}
