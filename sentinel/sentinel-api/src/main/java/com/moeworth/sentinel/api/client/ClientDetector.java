package com.moeworth.sentinel.api.client;

import com.moeworth.sentinel.api.check.CheckContext;

/**
 * Abstraction permettant de determiner le type de client d'un joueur.
 * Chaque plateforme (Bungee, Velocity, Paper...) fournit sa propre
 * implementation, en fonction des informations qu'elle est capable
 * d'extraire (canaux de plugin, handshake, forge marker, etc.).
 */
public interface ClientDetector {

    /**
     * Analyse le contexte de connexion et retourne les informations client
     * detectees. Doit retourner {@link ClientType#INCONNU} plutot que de lever
     * une exception lorsque la detection echoue.
     */
    ClientInfo detect(CheckContext context);
}
