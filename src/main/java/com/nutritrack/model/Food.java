package com.nutritrack.model;

import java.math.BigDecimal;

public record Food(
        long id,
        String name,
        String category,
        BigDecimal caloriesPer100g,
        BigDecimal proteinPer100g,
        BigDecimal carbohydratesPer100g,
        BigDecimal fatPer100g
) {
}
