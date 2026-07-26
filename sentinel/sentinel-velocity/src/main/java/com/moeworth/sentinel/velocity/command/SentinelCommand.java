package com.moeworth.sentinel.velocity.command;

import com.moeworth.sentinel.api.model.TrustReport;
import com.moeworth.sentinel.core.SentinelCore;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

/**
 * Commande "/sentinel <joueur>" affichant le dernier rapport de confiance
 * connu pour un joueur (equivalent Velocity de sentinel-bungee.SentinelCommand).
 */
public final class SentinelCommand implements SimpleCommand {

    private final SentinelCore core;
    private final ProxyServer server;

    public SentinelCommand(SentinelCore core, ProxyServer server) {
        this.core = core;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        var source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 1) {
            source.sendMessage(Component.text("Usage : /sentinel <joueur>", NamedTextColor.RED));
            return;
        }

        Optional<com.velocitypowered.api.proxy.Player> target = server.getPlayer(args[0]);
        if (target.isEmpty()) {
            source.sendMessage(Component.text("Joueur introuvable (doit etre connecte pour l'instant).", NamedTextColor.RED));
            return;
        }

        try {
            Optional<TrustReport> report = core.storage().findLatest(target.get().getUniqueId());
            if (report.isEmpty()) {
                source.sendMessage(Component.text("Aucun rapport de confiance disponible pour ce joueur.", NamedTextColor.YELLOW));
                return;
            }
            printReport(source, report.get());
        } catch (Exception e) {
            source.sendMessage(Component.text("Erreur lors de la lecture du rapport : " + e.getMessage(), NamedTextColor.RED));
        }
    }

    private void printReport(com.velocitypowered.api.command.CommandSource source, TrustReport report) {
        source.sendMessage(Component.text("=== Rapport Sentinel : " + report.playerName() + " ===", NamedTextColor.GOLD));
        source.sendMessage(Component.text("Score global : " + report.globalScore() + "/10", NamedTextColor.AQUA));
        source.sendMessage(Component.text("Niveau de risque : " + report.riskLevel(), NamedTextColor.AQUA));
        for (var result : report.results()) {
            source.sendMessage(Component.text(" - " + result.displayName() + " : "
                    + result.status() + " (" + result.reason() + ")", NamedTextColor.GRAY));
        }
    }
}
