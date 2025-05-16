package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Fetch environment variables directly using System.getenv()
    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        // Make sure the environment variables are set properly
        if (URL == null || USER == null || PASSWORD == null) {
            throw new SQLException("Database connection environment variables are not set.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
