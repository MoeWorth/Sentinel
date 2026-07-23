package com.moeworth.sentinel.core;

import com.moeworth.sentinel.api.provider.NetworkThreatService;
import com.moeworth.sentinel.api.provider.ReputationProvider;
import com.moeworth.sentinel.api.storage.TrustStorage;
import com.moeworth.sentinel.common.cache.SimpleCache;
import com.moeworth.sentinel.common.config.SentinelConfig;
import com.moeworth.sentinel.core.checks.ClientBrandCheck;
import com.moeworth.sentinel.core.checks.NetworkThreatCheck;
import com.moeworth.sentinel.core.checks.PremiumAccountCheck;
import com.moeworth.sentinel.core.checks.ReputationCheck;
import com.moeworth.sentinel.core.engine.ScoringService;
import com.moeworth.sentinel.core.engine.TrustAnalysisEngine;
import com.moeworth.sentinel.core.storage.SQLiteTrustStorage;

import java.net.InetAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * Point d'entree/bootstrap du coeur Sentinel, instancie par chaque module
 * de plateforme (sentinel-bungee, futur sentinel-paper, ...).
 * <p>
 * Cette classe assemble le moteur d'analyse avec les verifications par
 * defaut. Le module de plateforme peut ensuite enregistrer des verifications
 * ou fournisseurs supplementaires via {@link #engine()}.
 */
public final class SentinelCore {

    private final SentinelConfig config;
    private final TrustAnalysisEngine engine;
    private final TrustStorage storage;

    private SentinelCore(SentinelConfig config, TrustAnalysisEngine engine, TrustStorage storage) {
        this.config = config;
        this.engine = engine;
        this.storage = storage;
    }

    /**
     * Construit une instance complete du coeur Sentinel.
     *
     * @param config              configuration chargee (config.yml)
     * @param dataFolder          dossier de donnees de la plateforme (pour le fichier SQLite)
     * @param networkThreatService implementation de detection VPN/Proxy/Tor a utiliser
     * @param reputationProviders liste des fournisseurs de reputation configures
     */
    public static SentinelCore bootstrap(SentinelConfig config,
                                          Path dataFolder,
                                          NetworkThreatService networkThreatService,
                                          List<ReputationProvider> reputationProviders) throws TrustStorage.StorageException {
        ScoringService scoringService = new ScoringService(config);
        TrustAnalysisEngine engine = new TrustAnalysisEngine(config, scoringService);

        engine.registerCheck(new PremiumAccountCheck());
        engine.registerCheck(new ClientBrandCheck());
        engine.registerCheck(new ReputationCheck(reputationProviders));

        SimpleCache<InetAddress, NetworkThreatService.NetworkThreatResult> networkCache =
                new SimpleCache<>(Duration.ofMinutes(30));
        engine.registerCheck(new NetworkThreatCheck(networkThreatService, networkCache));

        TrustStorage storage = new SQLiteTrustStorage(dataFolder.resolve("sentinel.db"));
        storage.initialize();

        return new SentinelCore(config, engine, storage);
    }

    public TrustAnalysisEngine engine() {
        return engine;
    }

    public TrustStorage storage() {
        return storage;
    }

    public SentinelConfig config() {
        return config;
    }

    public void shutdown() {
        storage.close();
    }
}
