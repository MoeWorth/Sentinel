package com.moeworth.sentinel.api.client;

import java.util.List;

/**
 * Informations detectees a propos du client d'un joueur.
 *
 * @param type           type de client identifie
 * @param minecraftVersion version Minecraft rapportee (ex: "1.21.1")
 * @param brand          contenu brut du canal minecraft:brand
 * @param mods           liste des mods detectes via le protocole, si disponible (vide sinon)
 */
public record ClientInfo(
        ClientType type,
        String minecraftVersion,
        String brand,
        List<String> mods
) {
}
