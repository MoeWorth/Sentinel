package com.moeworth.sentinel.api.client;

/**
 * Types de client Minecraft identifiables via le Client Brand,
 * les canaux de plugin (mods list), ou des signatures reseau connues.
 */
public enum ClientType {
    VANILLA,
    FORGE,
    NEOFORGE,
    FABRIC,
    QUILT,
    LABYMOD,
    LUNAR_CLIENT,
    BADLION,
    FEATHER,
    ESSENTIAL,
    INCONNU
}
