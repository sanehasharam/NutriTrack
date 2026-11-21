package com.nutritrack.model;

import java.util.Arrays;
import java.util.List;

public enum FoodCategory {
    FRUITS("Fruits"),
    VEGETABLES("Vegetables"),
    DAIRY("Dairy"),
    GRAINS("Grains"),
    PROTEIN("Protein"),
    FAST_FOOD("Fast Food"),
    BEVERAGES("Beverages");

    private final String label;

    FoodCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static List<FoodCategory> valuesList() {
        return List.copyOf(Arrays.asList(values()));
    }

    public static FoodCategory fromLabel(String label) {
        return Arrays.stream(values())
                .filter(category -> category.label.equalsIgnoreCase(label.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Choose one of: " + String.join(", ",
                                Arrays.stream(values()).map(FoodCategory::label).toList()) + "."));
    }
}
