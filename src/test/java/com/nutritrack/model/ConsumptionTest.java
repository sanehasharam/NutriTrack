package com.nutritrack.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsumptionTest {
    @Test
    void scalesNutritionValuesToConsumedQuantity() {
        Food rice = new Food(1, "Rice", "Grain", new BigDecimal("130"),
                new BigDecimal("2.7"), new BigDecimal("28"), new BigDecimal("0.3"));
        Consumption consumption = new Consumption(1, rice, MealCategory.LUNCH,
                new BigDecimal("150"), LocalDateTime.now());

        assertEquals(new BigDecimal("195"), consumption.calories());
        assertEquals(new BigDecimal("4.05"), consumption.protein());
        assertEquals(new BigDecimal("42"), consumption.carbohydrates());
        assertEquals(new BigDecimal("0.45"), consumption.fat());
    }
}
