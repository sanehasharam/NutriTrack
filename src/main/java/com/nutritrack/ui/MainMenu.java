package com.nutritrack.ui;

import com.nutritrack.model.Food;
import com.nutritrack.model.FoodCategory;
import com.nutritrack.model.MealCategory;
import com.nutritrack.model.NutritionSummary;
import com.nutritrack.model.NutritionTarget;
import com.nutritrack.model.ServingSize;
import com.nutritrack.model.User;
import com.nutritrack.service.FoodService;
import com.nutritrack.service.NutritionService;
import com.nutritrack.service.ReportService;
import com.nutritrack.service.UserService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainMenu {
    private final UserService users;
    private final FoodService foods;
    private final NutritionService nutrition;
    private final ReportService reports;
    private final Scanner scanner = new Scanner(System.in);
    private boolean endOfInput;

    public MainMenu(UserService users, FoodService foods, NutritionService nutrition, ReportService reports) {
        this.users = users;
        this.foods = foods;
        this.nutrition = nutrition;
        this.reports = reports;
    }

    public void run() {
        System.out.println("Welcome to NutriTrack");
        while (true) {
            try {
                System.out.println("\n1. Register\n2. Log in\n0. Exit");
                switch (readInt("Choose: ")) {
                    case 1 -> register();
                    case 2 -> users.login(read("Email: "), read("Password: ").toCharArray())
                            .ifPresentOrElse(this::userMenu, () -> System.out.println("Invalid email or password."));
                    case 0 -> {
                        System.out.println("Goodbye.");
                        return;
                    }
                    default -> System.out.println("Choose one of the listed options.");
                }
            } catch (SQLException | IllegalArgumentException exception) {
                System.out.println("Error: " + exception.getMessage());
            }
        }
    }

    private void register() throws SQLException {
        User user = users.register(read("Name: "), read("Email: "), read("Password: ").toCharArray());
        System.out.println("Account created for " + user.name() + ". You can now log in.");
    }

    private void userMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            try {
                System.out.println("\nLogged in as " + user.name());
                System.out.println("1. View foods  2. Advanced food search  3. Add food  4. Update food");
                System.out.println("5. Delete food  6. Log consumption  7. Daily report");
                System.out.println("8. Set nutrition targets  9. Export daily report  0. Log out");
                switch (readInt("Choose: ")) {
                    case 1 -> printFoods(foods.all());
                    case 2 -> searchFoods();
                    case 3 -> addFood();
                    case 4 -> updateFood();
                    case 5 -> deleteFood();
                    case 6 -> logConsumption(user);
                    case 7 -> showReport(user);
                    case 8 -> saveTargets(user);
                    case 9 -> exportReport(user);
                    case 0 -> loggedIn = false;
                    default -> System.out.println("Choose one of the listed options.");
                }
            } catch (SQLException | IllegalArgumentException | IOException exception) {
                System.out.println("Error: " + exception.getMessage());
            }
        }
    }

    private void addFood() throws SQLException {
        FoodCategory category = readCategory();
        if (category == null) {
            return;
        }
        Food food = foods.add(read("Food name: "), category.label(), readDecimal("Calories per 100g: "),
                readDecimal("Protein per 100g (g): "), readDecimal("Carbohydrates per 100g (g): "),
                readDecimal("Fat per 100g (g): "), readOptionalDecimal("1 cup grams (blank if unknown): "),
                readOptionalDecimal("1 piece grams (blank if unknown): "),
                readOptionalDecimal("1 serving grams (blank if unknown): "));
        System.out.println("Added food with ID " + food.id() + ".");
    }

    private void updateFood() throws SQLException {
        long id = readLong("Food ID to update: ");
        FoodCategory category = readCategory();
        if (category == null) {
            return;
        }
        boolean updated = foods.update(id, read("New name: "), category.label(),
                readDecimal("Calories per 100g: "), readDecimal("Protein per 100g (g): "),
                readDecimal("Carbohydrates per 100g (g): "), readDecimal("Fat per 100g (g): "),
                readOptionalDecimal("1 cup grams (blank if unknown): "),
                readOptionalDecimal("1 piece grams (blank if unknown): "),
                readOptionalDecimal("1 serving grams (blank if unknown): "));
        System.out.println(updated ? "Food updated." : "No food found with that ID.");
    }

    private void deleteFood() throws SQLException {
        boolean deleted = foods.delete(readLong("Food ID to delete: "));
        System.out.println(deleted ? "Food deleted." : "No food found with that ID.");
    }

    private void logConsumption(User user) throws SQLException {
        Food food = foods.findById(readLong("Food ID: "));
        List<ServingSize> servingSizes = food.availableServingSizes();
        System.out.println("Serving sizes for " + food.name() + ":");
        for (int index = 0; index < servingSizes.size(); index++) {
            ServingSize servingSize = servingSizes.get(index);
            System.out.printf("%d. %s (%.2f g)%n", index + 1, servingSize.label(),
                    food.gramsFor(servingSize));
        }
        ServingSize servingSize = readServingSize(servingSizes);
        if (servingSize == null) {
            return;
        }
        BigDecimal quantity = food.gramsFor(servingSize)
                .multiply(readDecimal("Number of " + servingSize.label() + ": "));
        MealCategory meal = readMeal();
        nutrition.log(user.id(), food.id(), meal, quantity);
        System.out.printf("Consumption logged: %s %s (%.2f g).%n",
                quantity.divide(food.gramsFor(servingSize), 2, java.math.RoundingMode.HALF_UP),
                servingSize.label(), quantity);
    }

    private void searchFoods() throws SQLException {
        String name = read("Name contains or exact food ID (blank for any): ");
        if (name.trim().matches("\\d+")) {
            printFoods(foods.search(name));
            return;
        }
        FoodCategory category = readOptionalCategory();
        BigDecimal minimumProtein = readOptionalDecimal("Minimum protein per 100 g (blank for any): ");
        BigDecimal maximumCalories = readOptionalDecimal("Maximum calories per 100 g (blank for any): ");
        ServingSize servingSize = readOptionalServingSize();
        printFoods(foods.searchFoods(name, category, minimumProtein, maximumCalories, servingSize), servingSize);
    }

    private FoodCategory readCategory() {
        while (true) {
            if (endOfInput) {
                return null;
            }
            String input = read("Category (" + categoryLabels() + "): ");
            try {
                return FoodCategory.fromLabel(input);
            } catch (IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private FoodCategory readOptionalCategory() {
        List<FoodCategory> categories = foods.categories();
        System.out.println("Categories: 0. Any");
        for (int index = 0; index < categories.size(); index++) {
            System.out.printf("%d. %s%n", index + 1, categories.get(index).label());
        }
        while (true) {
            if (endOfInput) {
                return null;
            }
            int choice = readInt("Category: ");
            if (choice == 0) {
                return null;
            }
            if (choice > 0 && choice <= categories.size()) {
                return categories.get(choice - 1);
            }
            System.out.println("Choose one of the listed categories.");
        }
    }

    private String categoryLabels() {
        return String.join(", ", foods.categories().stream().map(FoodCategory::label).toList());
    }

    private ServingSize readServingSize(List<ServingSize> servingSizes) {
        while (true) {
            if (endOfInput) {
                return null;
            }
            int choice = readInt("Choose serving size: ");
            if (choice > 0 && choice <= servingSizes.size()) {
                return servingSizes.get(choice - 1);
            }
            System.out.println("Choose one of the listed serving sizes.");
        }
    }

    private ServingSize readOptionalServingSize() {
        List<ServingSize> servingSizes = List.of(
                ServingSize.ONE_CUP, ServingSize.ONE_PIECE, ServingSize.ONE_SERVING);
        System.out.println("Serving size: 0. Any");
        for (int index = 0; index < servingSizes.size(); index++) {
            System.out.printf("%d. %s%n", index + 1, servingSizes.get(index).label());
        }
        while (true) {
            if (endOfInput) {
                return null;
            }
            int choice = readInt("Serving size: ");
            if (choice == 0) {
                return null;
            }
            if (choice > 0 && choice <= servingSizes.size()) {
                return servingSizes.get(choice - 1);
            }
            System.out.println("Choose one of the listed serving sizes.");
        }
    }

    private void showReport(User user) throws SQLException {
        LocalDate date = readDate();
        NutritionSummary summary = nutrition.dailySummary(user.id(), date);
        reports.printDaily(user, date, summary, nutrition.targets(user.id()));
    }

    private void saveTargets(User user) throws SQLException {
        nutrition.saveTargets(user.id(), new NutritionTarget(readDecimal("Daily calories: "),
                readDecimal("Daily protein (g): "), readDecimal("Daily carbohydrates (g): "),
                readDecimal("Daily fat (g): ")));
        System.out.println("Daily targets saved.");
    }

    private void exportReport(User user) throws SQLException, IOException {
        LocalDate date = readDate();
        NutritionSummary summary = nutrition.dailySummary(user.id(), date);
        String path = read("CSV file path (for example daily-report.csv): ");
        reports.exportDaily(user, date, summary, nutrition.targets(user.id()), path);
        System.out.println("Report exported to " + path + ".");
    }

    private void printFoods(List<Food> foods) {
        printFoods(foods, null);
    }

    private void printFoods(List<Food> foods, ServingSize searchServingSize) {
        if (foods.isEmpty()) {
            System.out.println("No foods found.");
            return;
        }
        for (Food food : foods) {
            System.out.printf(
                    "#%d %-28s %-15s kcal %.2f | protein %.2f g | carbs %.2f g | fat %.2f g (per 100g)%n",
                    food.id(), food.name(), food.category(), food.caloriesPer100g(), food.proteinPer100g(),
                    food.carbohydratesPer100g(), food.fatPer100g());

            if (searchServingSize != null) {
                BigDecimal grams = food.gramsFor(searchServingSize);
                System.out.printf(
                        "   Nutrition for %s (%.2f g): kcal %.2f | protein %.2f g | carbs %.2f g | fat %.2f g%n",
                        searchServingSize.label(), grams,
                        scaleToGrams(food.caloriesPer100g(), grams),
                        scaleToGrams(food.proteinPer100g(), grams),
                        scaleToGrams(food.carbohydratesPer100g(), grams),
                        scaleToGrams(food.fatPer100g(), grams));
            }

            List<String> servingOptions = new ArrayList<>();
            if (food.cupGrams() != null) {
                servingOptions.add(String.format("1 cup %.2f g", food.cupGrams()));
            }
            if (food.pieceGrams() != null) {
                servingOptions.add(String.format("1 piece %.2f g", food.pieceGrams()));
            }
            if (food.servingGrams() != null) {
                servingOptions.add(String.format("1 serving %.2f g", food.servingGrams()));
            }
            if (!servingOptions.isEmpty()) {
                System.out.println("   Serving options: " + String.join(" | ", servingOptions));
            }
            System.out.println();
        }
    }

    private BigDecimal scaleToGrams(BigDecimal valuePer100g, BigDecimal grams) {
        return valuePer100g.multiply(grams).divide(BigDecimal.valueOf(100));
    }

    private MealCategory readMeal() {
        while (true) {
            String input = read("Meal (breakfast/lunch/dinner/snacks): ").trim().toUpperCase();
            try {
                return MealCategory.valueOf(input);
            } catch (IllegalArgumentException exception) {
                System.out.println("Enter breakfast, lunch, dinner, or snacks.");
            }
        }
    }

    private LocalDate readDate() {
        while (true) {
            try {
                String input = read("Date (YYYY-MM-DD, blank for today): ").trim();
                return input.isBlank() ? LocalDate.now() : LocalDate.parse(input);
            } catch (java.time.format.DateTimeParseException exception) {
                System.out.println("Enter the date in YYYY-MM-DD format.");
            }
        }
    }

    private String read(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            endOfInput = true;
            return "";
        }
        return scanner.nextLine();
    }

    private int readInt(String prompt) {
        while (true) {
            if (endOfInput) {
                return 0;
            }
            String input = read(prompt);
            if (endOfInput) {
                return 0;
            }
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    private long readLong(String prompt) {
        while (true) {
            if (endOfInput) {
                return 0;
            }
            try {
                return Long.parseLong(read(prompt).trim());
            } catch (NumberFormatException exception) {
                if (endOfInput) {
                    return 0;
                }
                System.out.println("Enter a whole number.");
            }
        }
    }

    private BigDecimal readDecimal(String prompt) {
        while (true) {
            if (endOfInput) {
                return BigDecimal.ZERO;
            }
            try {
                return new BigDecimal(read(prompt).trim());
            } catch (NumberFormatException exception) {
                if (endOfInput) {
                    return BigDecimal.ZERO;
                }
                System.out.println("Enter a valid number.");
            }
        }
    }

    private BigDecimal readOptionalDecimal(String prompt) {
        while (!endOfInput) {
            String input = read(prompt).trim();
            if (input.isBlank()) {
                return null;
            }
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid number or leave blank.");
            }
        }
        return null;
    }
}
