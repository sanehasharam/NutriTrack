package com.nutritrack.service;

import com.nutritrack.dao.FoodDao;
import com.nutritrack.model.Food;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FoodService {
    private final FoodDao foods;

    public FoodService(FoodDao foods) {
        this.foods = foods;
    }

    public List<Food> all() throws SQLException {
        return foods.findAll().stream().sorted(Comparator.comparing(Food::name,
                String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public List<Food> search(String query) throws SQLException {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Enter a food name or ID to search.");
        }
        String term = query.trim();
        if (term.matches("\\d+")) {
            Optional<Food> food = foods.findById(Long.parseLong(term));
            return food.map(List::of).orElseGet(List::of);
        }
        return foods.searchByName(term);
    }

    public Food add(String name, String category, BigDecimal calories, BigDecimal protein,
                    BigDecimal carbohydrates, BigDecimal fat) throws SQLException {
        return foods.create(validatedFood(0, name, category, calories, protein, carbohydrates, fat));
    }

    public boolean update(long id, String name, String category, BigDecimal calories, BigDecimal protein,
                          BigDecimal carbohydrates, BigDecimal fat) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Food ID must be positive.");
        }
        return foods.update(validatedFood(id, name, category, calories, protein, carbohydrates, fat));
    }

    public boolean delete(long id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Food ID must be positive.");
        }
        return foods.delete(id);
    }

    private Food validatedFood(long id, String name, String category, BigDecimal calories, BigDecimal protein,
                               BigDecimal carbohydrates, BigDecimal fat) {
        if (name == null || name.isBlank() || category == null || category.isBlank()) {
            throw new IllegalArgumentException("Food name and category are required.");
        }
        validateNonNegative("Calories", calories);
        validateNonNegative("Protein", protein);
        validateNonNegative("Carbohydrates", carbohydrates);
        validateNonNegative("Fat", fat);
        return new Food(id, name.trim(), category.trim(), calories, protein, carbohydrates, fat);
    }

    private void validateNonNegative(String label, BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(label + " must be zero or greater.");
        }
    }
}
