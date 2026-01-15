package com.ecom.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtils {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);
    private static HikariDataSource ds;

    private static synchronized void ensureInitialized() {
        if (ds != null) return;
        HikariConfig config = new HikariConfig();
        // Prefer system properties (so tests can set -DDB_URL=...), fall back to environment variables
        String url = System.getProperty("DB_URL");
        if (url == null) {
            url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/smartEcom");
        }
        String user = System.getProperty("DB_USERNAME");
        if (user == null) {
            user = System.getenv().getOrDefault("DB_USERNAME", "postgres");
        }
        String pass = System.getProperty("DB_PASSWORD");
        if (pass == null) {
            pass = System.getenv().getOrDefault("DB_PASSWORD", "Drake@7890");
        }

        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setPoolName("smart-ecom-pool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
        logger.info("Database pool initialized (URL: {})", url);
    }

    public static Connection getConnection() throws SQLException {
        ensureInitialized();
        return ds.getConnection();
    }

    private DatabaseUtils() {}
}
