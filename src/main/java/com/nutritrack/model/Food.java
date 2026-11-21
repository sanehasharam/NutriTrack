package com.nutritrack.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record Food(
        long id,
        String name,
        String category,
        BigDecimal caloriesPer100g,
        BigDecimal proteinPer100g,
        BigDecimal carbohydratesPer100g,
        BigDecimal fatPer100g,
        BigDecimal cupGrams,
        BigDecimal pieceGrams,
        BigDecimal servingGrams
) {
    public Food(long id, String name, String category, BigDecimal caloriesPer100g,
                BigDecimal proteinPer100g, BigDecimal carbohydratesPer100g, BigDecimal fatPer100g) {
        this(id, name, category, caloriesPer100g, proteinPer100g, carbohydratesPer100g, fatPer100g,
                null, null, null);
    }

    public List<ServingSize> availableServingSizes() {
        List<ServingSize> sizes = new ArrayList<>();
        sizes.add(ServingSize.ONE_HUNDRED_GRAMS);
        if (cupGrams != null) {
            sizes.add(ServingSize.ONE_CUP);
        }
        if (pieceGrams != null) {
            sizes.add(ServingSize.ONE_PIECE);
        }
        if (servingGrams != null) {
            sizes.add(ServingSize.ONE_SERVING);
        }
        return List.copyOf(sizes);
    }

    public BigDecimal gramsFor(ServingSize size) {
        return switch (size) {
            case ONE_HUNDRED_GRAMS -> BigDecimal.valueOf(100);
            case ONE_CUP -> requireWeight(cupGrams, size);
            case ONE_PIECE -> requireWeight(pieceGrams, size);
            case ONE_SERVING -> requireWeight(servingGrams, size);
        };
    }

    private BigDecimal requireWeight(BigDecimal grams, ServingSize size) {
        if (grams == null) {
            throw new IllegalArgumentException(size.label() + " is not defined for " + name + ".");
        }
        return grams;
    }
}
