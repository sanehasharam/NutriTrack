package com.nutritrack.ui;

import com.nutritrack.model.Food;
import com.nutritrack.model.MealCategory;
import com.nutritrack.model.NutritionSummary;
import com.nutritrack.model.NutritionTarget;
import com.nutritrack.model.User;
import com.nutritrack.service.FoodService;
import com.nutritrack.service.NutritionService;
import com.nutritrack.service.ReportService;
import com.nutritrack.service.UserService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class MainMenu {
    private final UserService users;
    private final FoodService foods;
    private final NutritionService nutrition;
    private final ReportService reports;
    private final Scanner scanner = new Scanner(System.in);

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
                System.out.println("1. View foods  2. Search food  3. Add food  4. Update food");
                System.out.println("5. Delete food  6. Log consumption  7. Daily report");
                System.out.println("8. Set nutrition targets  9. Export daily report  0. Log out");
                switch (readInt("Choose: ")) {
                    case 1 -> printFoods(foods.all());
                    case 2 -> printFoods(foods.search(read("Food name or ID: ")));
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
        Food food = foods.add(read("Food name: "), read("Category: "), readDecimal("Calories per 100g: "),
                readDecimal("Protein per 100g (g): "), readDecimal("Carbohydrates per 100g (g): "),
                readDecimal("Fat per 100g (g): "));
        System.out.println("Added food with ID " + food.id() + ".");
    }

    private void updateFood() throws SQLException {
        long id = readLong("Food ID to update: ");
        boolean updated = foods.update(id, read("New name: "), read("New category: "),
                readDecimal("Calories per 100g: "), readDecimal("Protein per 100g (g): "),
                readDecimal("Carbohydrates per 100g (g): "), readDecimal("Fat per 100g (g): "));
        System.out.println(updated ? "Food updated." : "No food found with that ID.");
    }

    private void deleteFood() throws SQLException {
        boolean deleted = foods.delete(readLong("Food ID to delete: "));
        System.out.println(deleted ? "Food deleted." : "No food found with that ID.");
    }

    private void logConsumption(User user) throws SQLException {
        long foodId = readLong("Food ID: ");
        MealCategory meal = readMeal();
        BigDecimal quantity = readDecimal("Quantity in grams: ");
        nutrition.log(user.id(), foodId, meal, quantity);
        System.out.println("Consumption logged.");
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
        if (foods.isEmpty()) {
            System.out.println("No foods found.");
            return;
        }
        foods.forEach(food -> System.out.printf(
                "#%d %-20s %-15s kcal %.2f | protein %.2f g | carbs %.2f g | fat %.2f g (per 100g)%n",
                food.id(), food.name(), food.category(), food.caloriesPer100g(), food.proteinPer100g(),
                food.carbohydratesPer100g(), food.fatPer100g()));
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
        return scanner.nextLine();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(read(prompt).trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    private long readLong(String prompt) {
        while (true) {
            try {
                return Long.parseLong(read(prompt).trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    private BigDecimal readDecimal(String prompt) {
        while (true) {
            try {
                return new BigDecimal(read(prompt).trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid number.");
            }
        }
    }
}
