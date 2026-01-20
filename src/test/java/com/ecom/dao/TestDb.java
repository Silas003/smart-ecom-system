package com.ecom.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class TestDb {
    public static void setupDb() throws Exception {
        // Configure H2 in-memory DB for tests
        System.setProperty("DB_URL", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        System.setProperty("DB_USERNAME", "sa");
        System.setProperty("DB_PASSWORD", "");

        // Execute schema SQL
        try (Connection conn = DriverManager.getConnection(System.getProperty("DB_URL"), System.getProperty("DB_USERNAME"), System.getProperty("DB_PASSWORD"))) {
            InputStream in = TestDb.class.getResourceAsStream("/sql/schema.sql");
            if (in == null) throw new RuntimeException("schema.sql not found in test resources");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                // Split on semicolon followed by newline to separate statements
                String[] statements = sb.toString().split(";\s*\n");
                try (Statement stmt = conn.createStatement()) {
                    for (String s : statements) {
                        String sql = s.trim();
                        if (sql.isEmpty()) continue;
                        stmt.execute(sql);
                    }
                }
            }
        }
    }
}

