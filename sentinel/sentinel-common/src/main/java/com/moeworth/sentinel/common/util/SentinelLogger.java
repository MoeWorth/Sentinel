package com.moeworth.sentinel.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fine couche au-dessus de SLF4J afin de garder un point d'entree unique
 * pour le logging de Sentinel, prefixe de maniere coherente sur toutes
 * les plateformes (Bungee utilise java.util.logging en interne, ce
 * wrapper permet d'homogeneiser le comportement).
 */
public final class SentinelLogger {

    private static final String PREFIX = "[Sentinel] ";

    private final Logger delegate;

    private SentinelLogger(Class<?> owner) {
        this.delegate = LoggerFactory.getLogger(owner);
    }

    public static SentinelLogger of(Class<?> owner) {
        return new SentinelLogger(owner);
    }

    public void info(String message, Object... args) {
        delegate.info(PREFIX + message, args);
    }

    public void warn(String message, Object... args) {
        delegate.warn(PREFIX + message, args);
    }

    public void error(String message, Throwable throwable) {
        delegate.error(PREFIX + message, throwable);
    }

    public void debug(String message, Object... args) {
        delegate.debug(PREFIX + message, args);
    }
}
