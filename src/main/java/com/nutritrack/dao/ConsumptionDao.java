package com.nutritrack.dao;

import com.nutritrack.database.DatabaseConnection;
import com.nutritrack.model.Consumption;
import com.nutritrack.model.Food;
import com.nutritrack.model.MealCategory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConsumptionDao {
    private final DatabaseConnection database;

    public ConsumptionDao(DatabaseConnection database) {
        this.database = database;
    }

    public void create(long userId, long foodId, MealCategory meal, java.math.BigDecimal quantity)
            throws SQLException {
        String sql = "INSERT INTO consumption_logs (user_id, food_id, meal_category, quantity_grams, consumed_at) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, userId);
            statement.setLong(2, foodId);
            statement.setString(3, meal.name());
            statement.setBigDecimal(4, quantity);
            statement.executeUpdate();
        }
    }

    public List<Consumption> findByUserAndDate(long userId, LocalDate date) throws SQLException {
        String sql = """
                SELECT c.id, c.meal_category, c.quantity_grams, c.consumed_at,
                       f.id AS food_id, f.name, f.category, f.calories_per_100g, f.protein_per_100g,
                       f.carbohydrates_per_100g, f.fat_per_100g
                FROM consumption_logs c JOIN foods f ON f.id = c.food_id
                WHERE c.user_id = ? AND c.consumed_at >= ? AND c.consumed_at < ?
                ORDER BY c.consumed_at
                """;
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setTimestamp(2, java.sql.Timestamp.valueOf(date.atStartOfDay()));
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            try (ResultSet result = statement.executeQuery()) {
                List<Consumption> consumptions = new ArrayList<>();
                while (result.next()) {
                    consumptions.add(map(result));
                }
                return consumptions;
            }
        }
    }

    private Consumption map(ResultSet result) throws SQLException {
        var food = new Food(result.getLong("food_id"), result.getString("name"), result.getString("category"),
                result.getBigDecimal("calories_per_100g"), result.getBigDecimal("protein_per_100g"),
                result.getBigDecimal("carbohydrates_per_100g"), result.getBigDecimal("fat_per_100g"));
        return new Consumption(result.getLong("id"), food,
                MealCategory.valueOf(result.getString("meal_category")),
                result.getBigDecimal("quantity_grams"), result.getTimestamp("consumed_at").toLocalDateTime());
    }
}
