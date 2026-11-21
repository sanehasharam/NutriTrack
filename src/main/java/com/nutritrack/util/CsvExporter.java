package com.nutritrack.util;

import com.nutritrack.model.Consumption;
import com.nutritrack.model.NutritionSummary;
import com.nutritrack.model.NutritionTarget;
import com.nutritrack.model.User;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public final class CsvExporter {
    private CsvExporter() {
    }

    public static void export(User user, LocalDate date, NutritionSummary summary,
                              NutritionTarget target, String fileName) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(fileName))) {
            writer.write("Date,User,Food,Meal,Quantity (g),Calories,Protein (g),Carbohydrates (g),Fat (g)");
            writer.newLine();
            for (Consumption entry : summary.consumptions()) {
                writer.write(String.join(",", csv(date.toString()), csv(user.name()), csv(entry.food().name()),
                        csv(entry.mealCategory().name()), entry.quantityGrams().toPlainString(),
                        entry.calories().toPlainString(), entry.protein().toPlainString(),
                        entry.carbohydrates().toPlainString(), entry.fat().toPlainString()));
                writer.newLine();
            }
            writer.write(String.join(",", "TOTAL", "", "", "", "", summary.calories().toPlainString(),
                    summary.protein().toPlainString(), summary.carbohydrates().toPlainString(),
                    summary.fat().toPlainString()));
            writer.newLine();
            if (target != null) {
                writer.write(String.join(",", "TARGET", "", "", "", "", target.calories().toPlainString(),
                        target.protein().toPlainString(), target.carbohydrates().toPlainString(),
                        target.fat().toPlainString()));
                writer.newLine();
            }
        }
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
