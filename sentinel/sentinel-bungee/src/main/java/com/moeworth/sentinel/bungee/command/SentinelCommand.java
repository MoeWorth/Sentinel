package com.moeworth.sentinel.bungee.command;

import com.moeworth.sentinel.api.model.TrustReport;
import com.moeworth.sentinel.core.SentinelCore;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Optional;
import java.util.UUID;

/**
 * Commande "/sentinel <joueur>" affichant le dernier rapport de confiance
 * connu pour un joueur.
 */
public final class SentinelCommand extends Command {

    private final SentinelCore core;

    public SentinelCommand(SentinelCore core) {
        super("sentinel", "sentinel.admin");
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage : /sentinel <joueur>");
            return;
        }

        ProxiedPlayer target = net.md_5.bungee.api.ProxyServer.getInstance().getPlayer(args[0]);
        UUID uuid = target != null ? target.getUniqueId() : null;
        if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable (doit etre connecte pour l'instant).");
            return;
        }

        try {
            Optional<TrustReport> report = core.storage().findLatest(uuid);
            if (report.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "Aucun rapport de confiance disponible pour ce joueur.");
                return;
            }
            printReport(sender, report.get());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Erreur lors de la lecture du rapport : " + e.getMessage());
        }
    }

    private void printReport(CommandSender sender, TrustReport report) {
        sender.sendMessage(ChatColor.GOLD + "=== Rapport Sentinel : " + report.playerName() + " ===");
        sender.sendMessage(ChatColor.AQUA + "Score global : " + ChatColor.WHITE + report.globalScore() + "/10");
        sender.sendMessage(ChatColor.AQUA + "Niveau de risque : " + ChatColor.WHITE + report.riskLevel());
        for (var result : report.results()) {
            sender.sendMessage(ChatColor.GRAY + " - " + result.displayName() + " : "
                    + result.status() + " (" + result.reason() + ")");
        }
    }
}
