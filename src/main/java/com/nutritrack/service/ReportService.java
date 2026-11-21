package com.nutritrack.service;

import com.nutritrack.model.Consumption;
import com.nutritrack.model.NutritionSummary;
import com.nutritrack.model.NutritionTarget;
import com.nutritrack.model.User;
import com.nutritrack.util.CsvExporter;

import java.io.IOException;
import java.time.LocalDate;

public class ReportService {
    public void printDaily(User user, LocalDate date, NutritionSummary summary, NutritionTarget target) {
        System.out.println("\n=====================================");
        System.out.println("       NUTRITRACK DAILY REPORT");
        System.out.println("=====================================");
        System.out.println("Date: " + date);
        System.out.println("User: " + user.name());
        System.out.println("\nFood Consumption:");
        System.out.printf("%-20s %-12s %-12s %-12s%n", "Food", "Meal", "Quantity", "Calories");
        System.out.println("----------------------------------------------------------------");
        for (Consumption entry : summary.consumptions()) {
            System.out.printf("%-20s %-12s %-12s %-12s%n",
                    entry.food().name(), entry.mealCategory(), entry.quantityGrams() + "g",
                    format(entry.calories()) + " kcal");
        }
        System.out.println("----------------------------------------------------------------");
        System.out.println("TOTAL NUTRITION");
        System.out.println("Calories       : " + format(summary.calories()) + " kcal");
        System.out.println("Protein        : " + format(summary.protein()) + " g");
        System.out.println("Carbohydrates  : " + format(summary.carbohydrates()) + " g");
        System.out.println("Fat            : " + format(summary.fat()) + " g");
        System.out.println("\nDAILY TARGET COMPARISON");
        if (target == null) {
            System.out.println("No daily targets set. Use the targets menu to configure them.");
        } else {
            printTarget("Calories", summary.calories(), target.calories(), "kcal");
            printTarget("Protein", summary.protein(), target.protein(), "g");
            printTarget("Carbohydrates", summary.carbohydrates(), target.carbohydrates(), "g");
            printTarget("Fat", summary.fat(), target.fat(), "g");
        }
        System.out.println("=====================================\n");
    }

    public void exportDaily(User user, LocalDate date, NutritionSummary summary, NutritionTarget target,
                            String path) throws IOException {
        CsvExporter.export(user, date, summary, target, path);
    }

    private void printTarget(String label, java.math.BigDecimal consumed,
                             java.math.BigDecimal target, String unit) {
        System.out.printf("%-15s: target %s %s, consumed %s %s, remaining %s %s%n",
                label, format(target), unit, format(consumed), unit,
                format(target.subtract(consumed).max(java.math.BigDecimal.ZERO)), unit);
    }

    private String format(java.math.BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
