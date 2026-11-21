package com.nutritrack.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FoodServingTest {
    @Test
    void exposesConfiguredServingsAndTheirGramWeights() {
        Food banana = new Food(1, "Banana", "Fruits", BigDecimal.valueOf(89), BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(150), BigDecimal.valueOf(118),
                BigDecimal.valueOf(118));

        assertEquals(4, banana.availableServingSizes().size());
        assertEquals(BigDecimal.valueOf(100), banana.gramsFor(ServingSize.ONE_HUNDRED_GRAMS));
        assertEquals(BigDecimal.valueOf(150), banana.gramsFor(ServingSize.ONE_CUP));
        assertEquals(BigDecimal.valueOf(118), banana.gramsFor(ServingSize.ONE_PIECE));
        assertEquals(BigDecimal.valueOf(118), banana.gramsFor(ServingSize.ONE_SERVING));
    }

    @Test
    void rejectsServingSizesWithoutKnownGramWeights() {
        Food food = new Food(1, "Custom food", "Grains", BigDecimal.TEN, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE);

        assertThrows(IllegalArgumentException.class, () -> food.gramsFor(ServingSize.ONE_CUP));
        assertEquals(1, food.availableServingSizes().size());
    }
}
