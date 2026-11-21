package com.nutritrack.model;

import java.math.BigDecimal;

public record NutritionTarget(
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbohydrates,
        BigDecimal fat
) {
}
