package com.moeworth.sentinel.common.config;

/**
 * Represente la configuration d'une verification individuelle telle que
 * definie dans config.yml sous "checks.<id>".
 *
 * @param enabled true si la verification doit s'executer
 * @param weight  poids applique a son impact sur le score global (peut etre negatif)
 */
public record WeightConfig(boolean enabled, double weight) {
    public static final WeightConfig DEFAULT = new WeightConfig(true, 1.0);
}
