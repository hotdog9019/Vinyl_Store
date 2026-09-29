package ru.mirea.project.db;

import jdk.jfr.DataAmount;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String Url = "jdbc:postgresql://localhost:5432/vinyl_store";
    private static final String User = "postgres";
    private static final String Password = "1234";

    public static Connection oneConnection() throws SQLException {
        return DriverManager.getConnection(Url, User, Password);
    }

    public static void main(String[] args) throws SQLException {
        Database.oneConnection();
    }

}
