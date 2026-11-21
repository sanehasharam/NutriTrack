package com.nutritrack.service;

import com.nutritrack.dao.FoodDao;
import com.nutritrack.model.FoodCategory;
import com.nutritrack.model.Food;
import com.nutritrack.model.ServingSize;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FoodService {
    public static final BigDecimal HIGH_PROTEIN_MINIMUM = BigDecimal.valueOf(20);
    public static final BigDecimal LOW_CALORIE_MAXIMUM = BigDecimal.valueOf(100);

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

    public List<Food> searchFoods(String name, FoodCategory category,
                                  boolean highProtein, boolean lowCalorie) throws SQLException {
        return searchFoods(name, category,
                highProtein ? HIGH_PROTEIN_MINIMUM : null,
                lowCalorie ? LOW_CALORIE_MAXIMUM : null, null);
    }

    public List<Food> searchFoods(String name, FoodCategory category, BigDecimal minimumProtein,
                                  BigDecimal maximumCalories, ServingSize servingSize) throws SQLException {
        String searchName = name == null || name.isBlank() ? null : name.trim();
        validateOptionalNonNegative("Minimum protein", minimumProtein);
        validateOptionalNonNegative("Maximum calories", maximumCalories);
        return foods.filter(searchName, category == null ? null : category.label(),
                minimumProtein, maximumCalories, servingSize);
    }

    public Food findById(long id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Food ID must be positive.");
        }
        return foods.findById(id).orElseThrow(() -> new IllegalArgumentException(
                "No active food found with ID " + id + "."));
    }

    public List<FoodCategory> categories() {
        return FoodCategory.valuesList();
    }

    public Food add(String name, String category, BigDecimal calories, BigDecimal protein,
                    BigDecimal carbohydrates, BigDecimal fat) throws SQLException {
        return add(name, category, calories, protein, carbohydrates, fat, null, null, null);
    }

    public Food add(String name, String category, BigDecimal calories, BigDecimal protein,
                    BigDecimal carbohydrates, BigDecimal fat, BigDecimal cupGrams,
                    BigDecimal pieceGrams, BigDecimal servingGrams) throws SQLException {
        return foods.create(validatedFood(0, name, category, calories, protein, carbohydrates, fat,
                cupGrams, pieceGrams, servingGrams));
    }

    public boolean update(long id, String name, String category, BigDecimal calories, BigDecimal protein,
                          BigDecimal carbohydrates, BigDecimal fat) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Food ID must be positive.");
        }
        Optional<Food> existing = foods.findById(id);
        if (existing.isEmpty()) {
            return false;
        }
        Food current = existing.get();
        return update(id, name, category, calories, protein, carbohydrates, fat,
                current.cupGrams(), current.pieceGrams(), current.servingGrams());
    }

    public boolean update(long id, String name, String category, BigDecimal calories, BigDecimal protein,
                          BigDecimal carbohydrates, BigDecimal fat, BigDecimal cupGrams,
                          BigDecimal pieceGrams, BigDecimal servingGrams) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Food ID must be positive.");
        }
        return foods.update(validatedFood(id, name, category, calories, protein, carbohydrates, fat,
                cupGrams, pieceGrams, servingGrams));
    }

    public boolean delete(long id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Food ID must be positive.");
        }
        return foods.delete(id);
    }

    private Food validatedFood(long id, String name, String category, BigDecimal calories, BigDecimal protein,
                               BigDecimal carbohydrates, BigDecimal fat, BigDecimal cupGrams,
                               BigDecimal pieceGrams, BigDecimal servingGrams) {
        if (name == null || name.isBlank() || category == null || category.isBlank()) {
            throw new IllegalArgumentException("Food name and category are required.");
        }
        FoodCategory foodCategory = FoodCategory.fromLabel(category);
        validateNonNegative("Calories", calories);
        validateNonNegative("Protein", protein);
        validateNonNegative("Carbohydrates", carbohydrates);
        validateNonNegative("Fat", fat);
        validateOptionalWeight("1 cup grams", cupGrams);
        validateOptionalWeight("1 piece grams", pieceGrams);
        validateOptionalWeight("1 serving grams", servingGrams);
        return new Food(id, name.trim(), foodCategory.label(), calories, protein, carbohydrates, fat,
                cupGrams, pieceGrams, servingGrams);
    }

    private void validateNonNegative(String label, BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(label + " must be zero or greater.");
        }
    }

    private void validateOptionalWeight(String label, BigDecimal value) {
        if (value != null && value.signum() <= 0) {
            throw new IllegalArgumentException(label + " must be greater than zero.");
        }
    }

    private void validateOptionalNonNegative(String label, BigDecimal value) {
        if (value != null && value.signum() < 0) {
            throw new IllegalArgumentException(label + " must be zero or greater.");
        }
    }
}
