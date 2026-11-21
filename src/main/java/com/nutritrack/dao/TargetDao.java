package com.nutritrack.dao;

import com.nutritrack.database.DatabaseConnection;
import com.nutritrack.model.NutritionTarget;

import java.sql.SQLException;
import java.util.Optional;

public class TargetDao {
    private final DatabaseConnection database;

    public TargetDao(DatabaseConnection database) {
        this.database = database;
    }

    public void save(long userId, NutritionTarget target) throws SQLException {
        String sql = "INSERT INTO daily_targets (user_id, calories, protein, carbohydrates, fat) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE calories = VALUES(calories), protein = VALUES(protein), "
                + "carbohydrates = VALUES(carbohydrates), fat = VALUES(fat)";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setBigDecimal(2, target.calories());
            statement.setBigDecimal(3, target.protein());
            statement.setBigDecimal(4, target.carbohydrates());
            statement.setBigDecimal(5, target.fat());
            statement.executeUpdate();
        }
    }

    public Optional<NutritionTarget> findByUser(long userId) throws SQLException {
        String sql = "SELECT calories, protein, carbohydrates, fat FROM daily_targets WHERE user_id = ?";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (var result = statement.executeQuery()) {
                return result.next()
                        ? Optional.of(new NutritionTarget(result.getBigDecimal("calories"),
                        result.getBigDecimal("protein"), result.getBigDecimal("carbohydrates"),
                        result.getBigDecimal("fat")))
                        : Optional.empty();
            }
        }
    }
}
