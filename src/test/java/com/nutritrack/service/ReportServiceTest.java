package com.nutritrack.service;

import com.nutritrack.model.NutritionSummary;
import com.nutritrack.model.NutritionTarget;
import com.nutritrack.model.User;
import com.nutritrack.model.Consumption;
import com.nutritrack.model.Food;
import com.nutritrack.model.MealCategory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {
    @Test
    void printsNutritionProgressAndDailySummary() {
        NutritionSummary summary = new NutritionSummary(List.of(),
                new BigDecimal("226"), new BigDecimal("4"), new BigDecimal("52.2"), new BigDecimal("0.6"));
        NutritionTarget target = new NutritionTarget(
                new BigDecimal("2000"), new BigDecimal("60"), new BigDecimal("250"), new BigDecimal("65"));

        String report = print(summary, target);

        assertTrue(report.contains("Remaining    : 1774 kcal"));
        assertTrue(report.contains("Progress     : 11.30%"));
        assertTrue(report.contains("Progress     : 6.67%"));
        assertTrue(report.contains("Progress     : 20.88%"));
        assertTrue(report.contains("Progress     : 0.92%"));
        assertTrue(report.contains("[##------------------]"));
        assertTrue(report.contains("Status       : BELOW TARGET"));
        assertTrue(report.contains("Overall Calorie Progress: 11.30%"));
    }

    @Test
    void reportsProgressAsUnavailableForZeroTargets() {
        NutritionSummary summary = new NutritionSummary(List.of(),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        NutritionTarget target = new NutritionTarget(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        String report = print(summary, target);

        assertTrue(report.contains("Progress     : N/A"));
        assertTrue(report.contains("[no target]"));
        assertTrue(report.contains("Status       : NO TARGET SET"));
        assertTrue(report.contains("Overall Calorie Progress: N/A"));
    }

    @Test
    void printsMealTotalsAndDailyInsights() {
        Food rice = new Food(1, "Rice", "Grains", new BigDecimal("130"),
                new BigDecimal("2.7"), new BigDecimal("28"), new BigDecimal("0.3"));
        List<Consumption> entries = List.of(
                new Consumption(1, rice, MealCategory.BREAKFAST, new BigDecimal("100"),
                        LocalDate.now().atStartOfDay()),
                new Consumption(2, rice, MealCategory.SNACKS, new BigDecimal("50"),
                        LocalDate.now().atStartOfDay()));
        NutritionSummary summary = new NutritionSummary(entries, new BigDecimal("195"),
                new BigDecimal("4.05"), new BigDecimal("42"), new BigDecimal("0.45"));
        NutritionTarget target = new NutritionTarget(
                new BigDecimal("2000"), new BigDecimal("60"), new BigDecimal("250"), new BigDecimal("65"));

        String report = print(summary, target);

        assertTrue(report.lines().anyMatch(line -> line.startsWith("Breakfast")
                && line.contains("130") && line.contains("2.7")));
        assertTrue(report.lines().anyMatch(line -> line.startsWith("Snacks")
                && line.contains("65") && line.contains("1.35")));
        assertTrue(report.contains("Targets achieved or exceeded: 0 of 4."));
        assertTrue(report.contains("Still below target: Calories, Protein, Carbohydrates, Fat."));
    }

    private String print(NutritionSummary summary, NutritionTarget target) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try (PrintStream captured = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            new ReportService().printDaily(
                    new User(1, "Saneha sharma", "saneha@example.com", ""),
                    LocalDate.of(2026, 11, 21), summary, target);
        } finally {
            System.setOut(originalOut);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
