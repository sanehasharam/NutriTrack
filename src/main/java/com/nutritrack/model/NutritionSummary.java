package com.nutritrack.model;

import java.math.BigDecimal;
import java.util.List;

public record NutritionSummary(
        List<Consumption> consumptions,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbohydrates,
        BigDecimal fat
) {
    public NutritionSummary {
        consumptions = List.copyOf(consumptions);
    }
}
