package com.nutritrack.dao;

import com.nutritrack.database.DatabaseConnection;
import com.nutritrack.model.Food;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoodDao {
    private static final String COLUMNS =
            "id, name, category, calories_per_100g, protein_per_100g, carbohydrates_per_100g, fat_per_100g";
    private final DatabaseConnection database;

    public FoodDao(DatabaseConnection database) {
        this.database = database;
    }

    public List<Food> findAll() throws SQLException {
        try (var connection = database.open();
             var statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM foods ORDER BY name");
             ResultSet result = statement.executeQuery()) {
            List<Food> foods = new ArrayList<>();
            while (result.next()) {
                foods.add(map(result));
            }
            return foods;
        }
    }

    public Optional<Food> findById(long id) throws SQLException {
        try (var connection = database.open();
             var statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM foods WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public List<Food> searchByName(String query) throws SQLException {
        try (var connection = database.open();
             var statement = connection.prepareStatement(
                     "SELECT " + COLUMNS + " FROM foods WHERE name LIKE ? ORDER BY name")) {
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

    public Food create(Food food) throws SQLException {
        String sql = "INSERT INTO foods (name, category, calories_per_100g, protein_per_100g, "
                + "carbohydrates_per_100g, fat_per_100g) VALUES (?, ?, ?, ?, ?, ?)";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setFields(statement, food, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Creating the food did not return an ID.");
                }
                return new Food(keys.getLong(1), food.name(), food.category(), food.caloriesPer100g(),
                        food.proteinPer100g(), food.carbohydratesPer100g(), food.fatPer100g());
            }
        }
    }

    public boolean update(Food food) throws SQLException {
        String sql = "UPDATE foods SET name = ?, category = ?, calories_per_100g = ?, protein_per_100g = ?, "
                + "carbohydrates_per_100g = ?, fat_per_100g = ? WHERE id = ?";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            setFields(statement, food, true);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        try (var connection = database.open();
             var statement = connection.prepareStatement("DELETE FROM foods WHERE id = ?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private void setFields(java.sql.PreparedStatement statement, Food food, boolean includeId) throws SQLException {
        statement.setString(1, food.name());
        statement.setString(2, food.category());
        statement.setBigDecimal(3, food.caloriesPer100g());
        statement.setBigDecimal(4, food.proteinPer100g());
        statement.setBigDecimal(5, food.carbohydratesPer100g());
        statement.setBigDecimal(6, food.fatPer100g());
        if (includeId) {
            statement.setLong(7, food.id());
        }
    }

    private Food map(ResultSet result) throws SQLException {
        return new Food(result.getLong("id"), result.getString("name"), result.getString("category"),
                result.getBigDecimal("calories_per_100g"), result.getBigDecimal("protein_per_100g"),
                result.getBigDecimal("carbohydrates_per_100g"), result.getBigDecimal("fat_per_100g"));
    }
}
