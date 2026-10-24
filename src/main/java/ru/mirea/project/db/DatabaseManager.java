package ru.mirea.project.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/vinyl_store";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "1234";
    private static final Properties DATABASE_PROPERTIES = loadDatabaseProperties();

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("VINYL_DB_URL", property("db.url", DEFAULT_URL));
        String user = System.getenv().getOrDefault("VINYL_DB_USER", property("db.user", DEFAULT_USER));
        String password = System.getenv().getOrDefault("VINYL_DB_PASSWORD", property("db.password", DEFAULT_PASSWORD));
        return DriverManager.getConnection(url, user, password);
    }

    private static String property(String name, String fallback) {
        return DATABASE_PROPERTIES.getProperty(name, fallback);
    }

    private static Properties loadDatabaseProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseManager.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
            // Environment variables and defaults still allow the application to start.
        }
        return properties;
    }
}
