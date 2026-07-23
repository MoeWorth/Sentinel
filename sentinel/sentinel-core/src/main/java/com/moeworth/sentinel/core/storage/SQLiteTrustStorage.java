package com.moeworth.sentinel.core.storage;

import com.moeworth.sentinel.api.model.CheckResult;
import com.moeworth.sentinel.api.model.CheckStatus;
import com.moeworth.sentinel.api.model.RiskLevel;
import com.moeworth.sentinel.api.model.TrustReport;
import com.moeworth.sentinel.api.storage.TrustStorage;
import com.moeworth.sentinel.common.util.SentinelLogger;

import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation par defaut de {@link TrustStorage} basee sur SQLite.
 * <p>
 * Le detail de chaque verification est serialise en une ligne par
 * CheckResult, reliee au rapport par une cle etrangere, ce qui permet de
 * reconstituer un {@link TrustReport} complet a la lecture.
 * <p>
 * Une implementation PostgreSQL peut etre ajoutee en implementant
 * {@link TrustStorage} sans modifier le reste du coeur (voir configuration
 * "storage.type" dans config.yml).
 */
public final class SQLiteTrustStorage implements TrustStorage {

    private static final SentinelLogger LOGGER = SentinelLogger.of(SQLiteTrustStorage.class);

    private final Path databaseFile;
    private Connection connection;

    public SQLiteTrustStorage(Path databaseFile) {
        this.databaseFile = databaseFile;
    }

    @Override
    public void initialize() throws StorageException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.toAbsolutePath());
            try (Statement st = connection.createStatement()) {
                st.execute("""
                        CREATE TABLE IF NOT EXISTS trust_report (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            player_uuid TEXT NOT NULL,
                            player_name TEXT NOT NULL,
                            global_score REAL NOT NULL,
                            risk_level TEXT NOT NULL,
                            generated_at TEXT NOT NULL
                        )
                        """);
                st.execute("""
                        CREATE TABLE IF NOT EXISTS check_result (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            report_id INTEGER NOT NULL,
                            check_id TEXT NOT NULL,
                            display_name TEXT NOT NULL,
                            status TEXT NOT NULL,
                            score_impact REAL NOT NULL,
                            reason TEXT NOT NULL,
                            FOREIGN KEY (report_id) REFERENCES trust_report(id)
                        )
                        """);
                st.execute("CREATE INDEX IF NOT EXISTS idx_trust_report_uuid ON trust_report(player_uuid)");
            }
            LOGGER.info("Base SQLite initialisee : {}", databaseFile);
        } catch (ClassNotFoundException | SQLException e) {
            throw new StorageException("Impossible d'initialiser la base SQLite", e);
        }
    }

    @Override
    public void save(TrustReport report) throws StorageException {
        String insertReport = """
                INSERT INTO trust_report (player_uuid, player_name, global_score, risk_level, generated_at)
                VALUES (?, ?, ?, ?, ?)
                """;
        String insertCheck = """
                INSERT INTO check_result (report_id, check_id, display_name, status, score_impact, reason)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try {
            connection.setAutoCommit(false);
            long reportId;
            try (PreparedStatement ps = connection.prepareStatement(insertReport, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, report.playerUuid().toString());
                ps.setString(2, report.playerName());
                ps.setDouble(3, report.globalScore());
                ps.setString(4, report.riskLevel().name());
                ps.setString(5, report.generatedAt().toString());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    reportId = keys.getLong(1);
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(insertCheck)) {
                for (CheckResult result : report.results()) {
                    ps.setLong(1, reportId);
                    ps.setString(2, result.checkId());
                    ps.setString(3, result.displayName());
                    ps.setString(4, result.status().name());
                    ps.setDouble(5, result.scoreImpact());
                    ps.setString(6, result.reason());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            throw new StorageException("Echec de la sauvegarde du rapport", e);
        } finally {
            restoreAutoCommit();
        }
    }

    @Override
    public Optional<TrustReport> findLatest(UUID playerUuid) throws StorageException {
        String selectReport = """
                SELECT id, player_name, global_score, risk_level, generated_at
                FROM trust_report WHERE player_uuid = ?
                ORDER BY generated_at DESC LIMIT 1
                """;
        try (PreparedStatement ps = connection.prepareStatement(selectReport)) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                long reportId = rs.getLong("id");
                TrustReport report = new TrustReport(
                        playerUuid,
                        rs.getString("player_name"),
                        rs.getDouble("global_score"),
                        RiskLevel.valueOf(rs.getString("risk_level")),
                        loadChecks(reportId),
                        Instant.parse(rs.getString("generated_at"))
                );
                return Optional.of(report);
            }
        } catch (SQLException e) {
            throw new StorageException("Echec de la lecture du rapport", e);
        }
    }

    private List<CheckResult> loadChecks(long reportId) throws SQLException {
        List<CheckResult> results = new ArrayList<>();
        String query = "SELECT check_id, display_name, status, score_impact, reason FROM check_result WHERE report_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new CheckResult(
                            rs.getString("check_id"),
                            rs.getString("display_name"),
                            CheckStatus.valueOf(rs.getString("status")),
                            rs.getDouble("score_impact"),
                            rs.getString("reason")
                    ));
                }
            }
        }
        return results;
    }

    private void rollbackQuietly() {
        try {
            if (connection != null) connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void restoreAutoCommit() {
        try {
            if (connection != null) connection.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la fermeture de la connexion SQLite", e);
        }
    }
}
