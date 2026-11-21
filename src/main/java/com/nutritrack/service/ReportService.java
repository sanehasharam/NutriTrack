package com.nutritrack.service;

import com.nutritrack.model.Consumption;
import com.nutritrack.model.MealCategory;
import com.nutritrack.model.NutritionSummary;
import com.nutritrack.model.NutritionTarget;
import com.nutritrack.model.User;
import com.nutritrack.util.CsvExporter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportService {
    private static final String RULE = "========================================================";
    private static final int PROGRESS_BAR_WIDTH = 20;

    public void printDaily(User user, LocalDate date, NutritionSummary summary, NutritionTarget target) {
        List<NutrientProgress> nutrients = nutrientProgress(summary, target);

        System.out.println("\n" + RULE);
        System.out.println("              NUTRITRACK - DAILY REPORT");
        System.out.println(RULE);
        System.out.println("Date: " + date + "    User: " + user.name());

        System.out.println("\nNUTRITION PROGRESS");
        nutrients.forEach(this::printProgress);
        if (target == null) {
            System.out.println("No daily targets set. Use the targets menu to configure them.");
        }

        printMealAnalytics(summary);

        System.out.println("\n" + RULE);
        System.out.println("                 DAILY SUMMARY");
        System.out.println(RULE);
        System.out.println("Total Calories Consumed : " + format(summary.calories()) + " kcal");
        System.out.println("Overall Calorie Progress: " + progress(nutrients.get(0)));

        printInsights(nutrients);
        printConsumptions(summary);
        System.out.println("\nKeep tracking your nutrition throughout the day!");
        System.out.println(RULE + "\n");
    }

    public void exportDaily(User user, LocalDate date, NutritionSummary summary, NutritionTarget target,
                            String path) throws IOException {
        CsvExporter.export(user, date, summary, target, path);
    }

    private List<NutrientProgress> nutrientProgress(NutritionSummary summary, NutritionTarget target) {
        return List.of(
                new NutrientProgress("Calories", summary.calories(), target == null ? null : target.calories(), "kcal"),
                new NutrientProgress("Protein", summary.protein(), target == null ? null : target.protein(), "g"),
                new NutrientProgress("Carbohydrates", summary.carbohydrates(),
                        target == null ? null : target.carbohydrates(), "g"),
                new NutrientProgress("Fat", summary.fat(), target == null ? null : target.fat(), "g")
        );
    }

    private void printProgress(NutrientProgress nutrient) {
        System.out.println("\n" + nutrient.name());
        System.out.println("Target       : "
                + (nutrient.target() == null ? "Not set" : format(nutrient.target()) + " " + nutrient.unit()));
        System.out.println("Consumed     : " + format(nutrient.consumed()) + " " + nutrient.unit());
        System.out.println("Remaining    : " + remaining(nutrient));
        System.out.println("Progress     : " + progress(nutrient));
        System.out.println("              " + progressBar(nutrient));
        System.out.println("Status       : " + status(nutrient));
    }

    private String remaining(NutrientProgress nutrient) {
        if (nutrient.target() == null) {
            return "N/A";
        }
        return format(nutrient.target().subtract(nutrient.consumed()).max(BigDecimal.ZERO))
                + " " + nutrient.unit();
    }

    private String progress(NutrientProgress nutrient) {
        if (nutrient.target() == null || nutrient.target().signum() <= 0) {
            return "N/A";
        }
        BigDecimal percent = nutrient.consumed().multiply(BigDecimal.valueOf(100))
                .divide(nutrient.target(), 2, RoundingMode.HALF_UP);
        return String.format(Locale.ROOT, "%.2f%%", percent);
    }

    private String progressBar(NutrientProgress nutrient) {
        if (nutrient.target() == null || nutrient.target().signum() <= 0) {
            return "[no target]";
        }
        int filled = nutrient.consumed().max(BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(PROGRESS_BAR_WIDTH))
                .divide(nutrient.target(), 0, RoundingMode.FLOOR)
                .min(BigDecimal.valueOf(PROGRESS_BAR_WIDTH))
                .intValue();
        return "[" + "#".repeat(filled) + "-".repeat(PROGRESS_BAR_WIDTH - filled) + "]";
    }

    private String status(NutrientProgress nutrient) {
        if (nutrient.target() == null || nutrient.target().signum() <= 0) {
            return "NO TARGET SET";
        }
        int comparison = nutrient.consumed().compareTo(nutrient.target());
        if (comparison < 0) {
            return "BELOW TARGET";
        }
        return comparison == 0 ? "ACHIEVED" : "EXCEEDED";
    }

    private void printMealAnalytics(NutritionSummary summary) {
        System.out.println("\nMEAL-WISE ANALYTICS");
        System.out.printf("%-12s %14s %12s %12s %9s%n",
                "Meal", "Calories", "Protein", "Carbs", "Fat");
        System.out.println("--------------------------------------------------------------------------");
        for (MealCategory meal : MealCategory.values()) {
            List<Consumption> entries = summary.consumptions().stream()
                    .filter(entry -> entry.mealCategory() == meal)
                    .toList();
            System.out.printf("%-12s %14s %12s %12s %9s%n",
                    mealLabel(meal), format(sum(entries, Consumption::calories)),
                    format(sum(entries, Consumption::protein)),
                    format(sum(entries, Consumption::carbohydrates)),
                    format(sum(entries, Consumption::fat)));
        }
    }

    private String mealLabel(MealCategory meal) {
        return switch (meal) {
            case BREAKFAST -> "Breakfast";
            case LUNCH -> "Lunch";
            case DINNER -> "Dinner";
            case SNACKS -> "Snacks";
        };
    }

    private BigDecimal sum(List<Consumption> entries,
                           java.util.function.Function<Consumption, BigDecimal> amount) {
        return entries.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void printInsights(List<NutrientProgress> nutrients) {
        System.out.println("\nDAILY INSIGHTS");
        List<NutrientProgress> configured = nutrients.stream()
                .filter(nutrient -> nutrient.target() != null && nutrient.target().signum() > 0)
                .toList();
        if (configured.isEmpty()) {
            System.out.println("Set positive daily targets to receive nutrition progress insights.");
            return;
        }

        List<String> below = new ArrayList<>();
        int achievedOrExceeded = 0;
        for (NutrientProgress nutrient : configured) {
            if (nutrient.consumed().compareTo(nutrient.target()) < 0) {
                below.add(nutrient.name());
            } else {
                achievedOrExceeded++;
            }
        }
        System.out.printf("Targets achieved or exceeded: %d of %d.%n", achievedOrExceeded, configured.size());
        if (!below.isEmpty()) {
            System.out.println("Still below target: " + String.join(", ", below) + ".");
        } else {
            System.out.println("All configured nutrition targets have been achieved or exceeded.");
        }
    }

    private void printConsumptions(NutritionSummary summary) {
        System.out.println("\nFOOD CONSUMPTION");
        System.out.printf("%-28s %-12s %-14s %-12s%n", "Food", "Meal", "Quantity", "Calories");
        System.out.println("--------------------------------------------------------------------------");
        if (summary.consumptions().isEmpty()) {
            System.out.println("No food logged for this day.");
        } else {
            for (Consumption entry : summary.consumptions()) {
                System.out.printf("%-28s %-12s %-14s %s kcal%n",
                        entry.food().name(), entry.mealCategory(), entry.quantityGrams() + " g",
                        format(entry.calories()));
            }
        }
    }

    private String format(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private record NutrientProgress(String name, BigDecimal consumed, BigDecimal target, String unit) {
    }
}
