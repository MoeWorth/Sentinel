package com.moeworth.sentinel.api.check;

import com.moeworth.sentinel.api.client.ClientInfo;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Contexte transmis a chaque {@link TrustCheck} lors de son execution.
 * Regroupe toutes les informations disponibles sur la connexion en cours,
 * quelle que soit la plateforme (Bungee, Velocity, Paper, ...).
 *
 * @param playerUuid   UUID du joueur (peut etre un UUID hors-ligne selon le mode du serveur)
 * @param playerName   pseudo utilise pour la connexion
 * @param address      adresse IP de connexion du joueur
 * @param isPremium    true si le compte a ete verifie comme premium (Mojang), false sinon,
 *                     {@code null} si la verification n'est pas possible sur cette plateforme/config
 * @param protocolVersion version de protocole Minecraft envoyee par le client
 * @param clientBrand  contenu brut du canal "minecraft:brand", ou null si non recu
 * @param clientInfo   informations de client deja detectees en amont (peut etre null)
 */
public record CheckContext(
        UUID playerUuid,
        String playerName,
        InetAddress address,
        Boolean isPremium,
        int protocolVersion,
        String clientBrand,
        ClientInfo clientInfo
) {
}
