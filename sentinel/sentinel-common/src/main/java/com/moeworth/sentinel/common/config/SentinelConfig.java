package com.moeworth.sentinel.common.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Charge et expose la configuration YAML de Sentinel (config.yml).
 * <p>
 * Ce chargeur reste volontairement simple et generique (Map imbriquees) afin
 * de ne pas coupler sentinel-common a une bibliotheque de mapping YAML->POJO
 * particuliere. Les modules consommateurs (sentinel-core) exposent des
 * accesseurs typés au-dessus de cette structure brute si besoin.
 */
public final class SentinelConfig {

    private final Map<String, Object> raw;

    private SentinelConfig(Map<String, Object> raw) {
        this.raw = raw;
    }

    /** Charge la configuration depuis un fichier sur disque. */
    public static SentinelConfig loadFromFile(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return loadFromStream(in);
        }
    }

    /** Charge la configuration depuis un flux (ex: ressource embarquee du jar). */
    public static SentinelConfig loadFromStream(InputStream in) {
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(in);
        return new SentinelConfig(data != null ? data : new LinkedHashMap<>());
    }

    /**
     * Recupere le poids/etat d'une verification par son identifiant.
     * Retourne {@link WeightConfig#DEFAULT} si absent de la configuration.
     */
    @SuppressWarnings("unchecked")
    public WeightConfig checkWeight(String checkId) {
        Object checksNode = raw.get("checks");
        if (!(checksNode instanceof Map<?, ?> checks)) {
            return WeightConfig.DEFAULT;
        }
        Object entryNode = checks.get(checkId);
        if (!(entryNode instanceof Map<?, ?> entry)) {
            return WeightConfig.DEFAULT;
        }
        boolean enabled = (boolean) ((Map<String, Object>) entry).getOrDefault("enabled", true);
        double weight = ((Number) ((Map<String, Object>) entry).getOrDefault("weight", 1.0)).doubleValue();
        return new WeightConfig(enabled, weight);
    }

    /** Recupere un seuil de risque configure (ex: "thresholds.eleve" -> 5.0). */
    @SuppressWarnings("unchecked")
    public double threshold(String key, double defaultValue) {
        Object node = raw.get("thresholds");
        if (!(node instanceof Map<?, ?> thresholds)) {
            return defaultValue;
        }
        Object value = ((Map<String, Object>) thresholds).get(key);
        return value instanceof Number number ? number.doubleValue() : defaultValue;
    }

    /** Recupere une valeur arbitraire par chemin pointe ("storage.type"). */
    public Object get(String dottedPath) {
        Object current = raw;
        for (String part : dottedPath.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
        }
        return current;
    }

    /** Acces a la structure brute pour les besoins avances (modules de plateforme). */
    public Map<String, Object> raw() {
        return raw;
    }
}
