package com.nutritrack.dao;

import com.nutritrack.database.DatabaseConnection;
import com.nutritrack.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDao {
    private final DatabaseConnection database;

    public UserDao(DatabaseConnection database) {
        this.database = database;
    }

    public User create(String name, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Creating the user did not return an ID.");
                }
                return new User(keys.getLong(1), name, email, passwordHash);
            }
        }
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email, password_hash FROM users WHERE email = ?";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    private User map(ResultSet result) throws SQLException {
        return new User(result.getLong("id"), result.getString("name"),
                result.getString("email"), result.getString("password_hash"));
    }
}
