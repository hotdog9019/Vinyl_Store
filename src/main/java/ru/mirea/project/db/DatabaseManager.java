package ru.mirea.project.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/vinyl_store";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "1234";

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("VINYL_DB_URL", DEFAULT_URL);
        String user = System.getenv().getOrDefault("VINYL_DB_USER", DEFAULT_USER);
        String password = System.getenv().getOrDefault("VINYL_DB_PASSWORD", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }
}
