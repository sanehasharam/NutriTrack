package com.nutritrack.service;

import com.nutritrack.dao.UserDao;
import com.nutritrack.model.User;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

public class UserService {
    private final UserDao users;

    public UserService(UserDao users) {
        this.users = users;
    }

    public User register(String name, String email, char[] password) throws SQLException {
        String cleanName = name == null ? "" : name.trim();
        String cleanEmail = normalizeEmail(email);
        validateCredentials(cleanName, cleanEmail, password);
        if (users.findByEmail(cleanEmail).isPresent()) {
            throw new IllegalArgumentException("An account with that email already exists.");
        }
        return users.create(cleanName, cleanEmail, PasswordHasher.hash(password));
    }

    public Optional<User> login(String email, char[] password) throws SQLException {
        if (password == null || password.length == 0) {
            return Optional.empty();
        }
        return users.findByEmail(normalizeEmail(email))
                .filter(user -> PasswordHasher.verify(password, user.passwordHash()));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateCredentials(String name, String email, char[] password) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
    }
}
