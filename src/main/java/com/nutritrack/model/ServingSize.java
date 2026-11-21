package com.nutritrack.model;

public enum ServingSize {
    ONE_HUNDRED_GRAMS("100 grams"),
    ONE_CUP("1 cup"),
    ONE_PIECE("1 piece"),
    ONE_SERVING("1 serving");

    private final String label;

    ServingSize(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
