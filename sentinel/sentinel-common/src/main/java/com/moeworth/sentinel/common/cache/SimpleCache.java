package com.moeworth.sentinel.common.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Cache en memoire, minimaliste et sans dependance externe, a expiration
 * par entree (TTL). Utilise notamment pour eviter d'interroger a chaque
 * connexion les fournisseurs de reputation / detection VPN pour une meme
 * adresse IP recemment analysee.
 *
 * @param <K> type de cle
 * @param <V> type de valeur mise en cache
 */
public final class SimpleCache<K, V> {

    private record Entry<V>(V value, Instant expiresAt) {
        boolean expired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<K, Entry<V>> store = new ConcurrentHashMap<>();
    private final Duration ttl;

    public SimpleCache(Duration ttl) {
        this.ttl = ttl;
    }

    /** Retourne la valeur en cache, ou la calcule via {@code loader} si absente/expiree. */
    public V getOrCompute(K key, Function<K, V> loader) {
        Entry<V> existing = store.get(key);
        if (existing != null && !existing.expired()) {
            return existing.value();
        }
        V computed = loader.apply(key);
        store.put(key, new Entry<>(computed, Instant.now().plus(ttl)));
        return computed;
    }

    public void invalidate(K key) {
        store.remove(key);
    }

    public void clear() {
        store.clear();
    }
}
