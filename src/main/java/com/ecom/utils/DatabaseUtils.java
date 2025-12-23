package com.ecom.utils;
import org.postgresql.util.PSQLException;

import java.sql.*;

public class DatabaseUtils {
    private static final String driver = "org.postgresql.Driver";
    private static final String url ="jdbc:postgresql://localhost:5432/smartEcom";
    private static final String username = System.getenv("DB_USERNAME");
    private static final String password = System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws ClassNotFoundException,SQLException, PSQLException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,username,password);
        System.out.println("Connected to database");
        return connection;
    }
}
