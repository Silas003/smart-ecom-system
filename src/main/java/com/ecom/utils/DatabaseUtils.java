package com.ecom.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtils {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);
    private static final HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/smartEcom");
        String user = System.getenv().getOrDefault("DB_USERNAME", "postgres");
        String pass = System.getenv().getOrDefault("DB_PASSWORD", "Drake@7890");

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
        return ds.getConnection();
    }

    private DatabaseUtils() {}
}
