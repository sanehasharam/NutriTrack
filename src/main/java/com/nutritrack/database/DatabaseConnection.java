package com.nutritrack.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseConnection() {
        this(
                setting("NUTRITRACK_DB_URL", "jdbc:mysql://localhost:3306/nutritrack"),
                setting("NUTRITRACK_DB_USER", "root"),
                setting("NUTRITRACK_DB_PASSWORD", "")
        );
    }

    public DatabaseConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection open() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null ? defaultValue : value;
    }
}
