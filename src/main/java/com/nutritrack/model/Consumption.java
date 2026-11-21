package com.nutritrack.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Consumption(
        long id,
        Food food,
        MealCategory mealCategory,
        BigDecimal quantityGrams,
        LocalDateTime consumedAt
) {
    public BigDecimal calories() {
        return amount(food.caloriesPer100g());
    }

    public BigDecimal protein() {
        return amount(food.proteinPer100g());
    }

    public BigDecimal carbohydrates() {
        return amount(food.carbohydratesPer100g());
    }

    public BigDecimal fat() {
        return amount(food.fatPer100g());
    }

    private BigDecimal amount(BigDecimal per100g) {
        return per100g.multiply(quantityGrams).divide(BigDecimal.valueOf(100));
    }
}
