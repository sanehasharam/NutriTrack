
package com.nutritrack.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.nutritrack.database.DatabaseConnection;
import com.nutritrack.model.Food;
import com.nutritrack.model.ServingSize;

public class FoodDao {

    private static final String COLUMNS =
            "id, name, category, calories_per_100g, protein_per_100g, carbohydrates_per_100g, fat_per_100g, " +
                    "cup_grams, piece_grams, serving_grams";

    private final DatabaseConnection database;

    public FoodDao(DatabaseConnection database) {
        this.database = database;
    }

    // View all active foods
    public List<Food> findAll() throws SQLException {

        String sql = "SELECT " + COLUMNS +
                " FROM foods WHERE is_active = TRUE ORDER BY name";

        try (var connection = database.open();
             var statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            List<Food> foods = new ArrayList<>();

            while (result.next()) {
                foods.add(map(result));
            }

            return foods;
        }
    }

    // Find active food by ID
    public Optional<Food> findById(long id) throws SQLException {

        String sql = "SELECT " + COLUMNS +
                " FROM foods WHERE id = ? AND is_active = TRUE";

        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? Optional.of(map(result))
                        : Optional.empty();
            }
        }
    }

    // Search active foods by name
    public List<Food> searchByName(String query) throws SQLException {

        String sql = "SELECT " + COLUMNS +
                " FROM foods WHERE name LIKE ? AND is_active = TRUE ORDER BY name";

        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + query + "%");

            try (ResultSet result = statement.executeQuery()) {

                List<Food> foods = new ArrayList<>();

                while (result.next()) {
                    foods.add(map(result));
                }
                return foods;
            }
        }
    }

    public List<Food> filter(String name, String category, java.math.BigDecimal minimumProtein,
                             java.math.BigDecimal maximumCalories) throws SQLException {
        return filter(name, category, minimumProtein, maximumCalories, null);
    }

    public List<Food> filter(String name, String category, java.math.BigDecimal minimumProtein,
                             java.math.BigDecimal maximumCalories, ServingSize servingSize) throws SQLException {
        String servingColumn = servingColumn(servingSize);
        StringBuilder sql = new StringBuilder("SELECT ").append(COLUMNS)
                .append(" FROM foods WHERE is_active = TRUE");
        if (name != null && !name.isBlank()) {
            sql.append(" AND name LIKE ?");
        }
        if (category != null) {
            sql.append(" AND category = ?");
        }
        if (minimumProtein != null) {
            sql.append(" AND protein_per_100g >= ?");
        }
        if (maximumCalories != null) {
            sql.append(" AND calories_per_100g <= ?");
        }
        if (servingColumn != null) {
            sql.append(" AND ").append(servingColumn).append(" IS NOT NULL");
        }
        sql.append(" ORDER BY name");

        try (var connection = database.open();
             var statement = connection.prepareStatement(sql.toString())) {
            int parameter = 1;
            if (name != null && !name.isBlank()) {
                statement.setString(parameter++, "%" + name.trim() + "%");
            }
            if (category != null) {
                statement.setString(parameter++, category);
            }
            if (minimumProtein != null) {
                statement.setBigDecimal(parameter++, minimumProtein);
            }
            if (maximumCalories != null) {
                statement.setBigDecimal(parameter, maximumCalories);
            }
            try (ResultSet result = statement.executeQuery()) {
                List<Food> foods = new ArrayList<>();
                while (result.next()) {
                    foods.add(map(result));
                }
                return foods;
            }
        }
    }

    private String servingColumn(ServingSize servingSize) {
        if (servingSize == null) {
            return null;
        }
        return switch (servingSize) {
            case ONE_CUP -> "cup_grams";
            case ONE_PIECE -> "piece_grams";
            case ONE_SERVING -> "serving_grams";
            case ONE_HUNDRED_GRAMS -> throw new IllegalArgumentException(
                    "Serving-size search supports cup, piece, or serving.");
        };
    }

    // Add new food
    public Food create(Food food) throws SQLException {

        String sql = "INSERT INTO foods " +
                "(name, category, calories_per_100g, protein_per_100g, " +
                "carbohydrates_per_100g, fat_per_100g, cup_grams, piece_grams, serving_grams) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (var connection = database.open();
             var statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            setFields(statement, food, false);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new SQLException("Creating the food did not return an ID.");
                }

                return new Food(
                        keys.getLong(1),
                        food.name(),
                        food.category(),
                        food.caloriesPer100g(),
                        food.proteinPer100g(),
                        food.carbohydratesPer100g(),
                        food.fatPer100g(),
                        food.cupGrams(),
                        food.pieceGrams(),
                        food.servingGrams()
                );
            }
        }
    }

    // Update existing active food
    public boolean update(Food food) throws SQLException {

        String sql = "UPDATE foods SET " +
                "name = ?, category = ?, calories_per_100g = ?, " +
                "protein_per_100g = ?, carbohydrates_per_100g = ?, " +
                "fat_per_100g = ?, cup_grams = ?, piece_grams = ?, serving_grams = ? " +
                "WHERE id = ? AND is_active = TRUE";

        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {

            setFields(statement, food, true);

            return statement.executeUpdate() > 0;
        }
    }

    // Soft delete food
    public boolean delete(long id) throws SQLException {

        String sql = "UPDATE foods SET is_active = FALSE " +
                "WHERE id = ? AND is_active = TRUE";

        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    // Set food fields
    private void setFields(
            java.sql.PreparedStatement statement,
            Food food,
            boolean includeId
    ) throws SQLException {

        statement.setString(1, food.name());
        statement.setString(2, food.category());
        statement.setBigDecimal(3, food.caloriesPer100g());
        statement.setBigDecimal(4, food.proteinPer100g());
        statement.setBigDecimal(5, food.carbohydratesPer100g());
        statement.setBigDecimal(6, food.fatPer100g());
        setNullableDecimal(statement, 7, food.cupGrams());
        setNullableDecimal(statement, 8, food.pieceGrams());
        setNullableDecimal(statement, 9, food.servingGrams());

        if (includeId) {
            statement.setLong(10, food.id());
        }
    }

    private void setNullableDecimal(java.sql.PreparedStatement statement, int index,
                                    java.math.BigDecimal value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DECIMAL);
        } else {
            statement.setBigDecimal(index, value);
        }
    }

    // Convert database record into Food object
    private Food map(ResultSet result) throws SQLException {

        return new Food(
                result.getLong("id"),
                result.getString("name"),
                result.getString("category"),
                result.getBigDecimal("calories_per_100g"),
                result.getBigDecimal("protein_per_100g"),
                result.getBigDecimal("carbohydrates_per_100g"),
                result.getBigDecimal("fat_per_100g"),
                result.getBigDecimal("cup_grams"),
                result.getBigDecimal("piece_grams"),
                result.getBigDecimal("serving_grams")
        );
    }
}
