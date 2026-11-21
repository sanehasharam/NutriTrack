# NutriTrack

A Java 21 menu-driven prototype for food management and daily nutrition tracking, using MySQL and JDBC.

## Requirements

- JDK 21
- Maven 3.9+
- MySQL 8+

## Database setup

Run `database/schema.sql` with a MySQL account that can create a database and tables. This creates the tables and seeds a starter database of common foods. For an existing installation, back up the database, run `database/migrations/001_advanced_food_database.sql` once, then `database/migrations/002_protein_food_category.sql` to rename the Meat category to Protein, and finally run `database/schema.sql` to add any missing starter foods. These migrations alter existing records and add columns/indexes; they do not rebuild the table or add duplicate seed foods. Configure the application using:

| Environment variable | Default |
| --- | --- |
| `NUTRITRACK_DB_URL` | `jdbc:mysql://localhost:3306/nutritrack` |
| `NUTRITRACK_DB_USER` | `root` |
| `NUTRITRACK_DB_PASSWORD` | empty |

Set a non-empty database password in your local environment; credentials are not stored in source code.

## Run and test

```shell
mvn test
mvn exec:java
```

The first menu supports account registration and login. After login, advanced food search combines name, category, minimum protein, maximum calories, and serving-size filters. Protein and calorie limits are entered per 100 grams; a serving-size filter returns only foods with that configured size and displays the selected portion's nutrition. Categories include Fruits, Vegetables, Dairy, Grains, Protein, Fast Food, and Beverages. Nutrition values are stored per 100 grams as the reference; serving sizes convert cup, piece, or serving weights to grams before calculating logged nutrition. Serving options are shown only when their weights are known. Nutrient and serving weights for generic foods are reference estimates; recipes, preparation, brands, and actual portion sizes can vary. Food records referenced by consumption history cannot be deleted because of the database foreign key.

The daily report shows target progress with console bars, remaining amounts, achieved/exceeded status, meal-wise calorie and macro totals, and a short progress insight. Bars cap at 100%; percentages can exceed 100%. Zero or unset targets are shown as unavailable. The report can show past dates in `YYYY-MM-DD` format; press Enter for today.

Passwords are stored as salted PBKDF2 hashes. JDBC statements use parameters and resources use try-with-resources.

## CA-3 implementation and test checklist

- User registration and login; food create/read/search/update/delete.
- Consumption records associate a user, food, meal category, quantity, and timestamp.
- Daily calories and macros are calculated from the food's per-100g values; targets include remaining intake.
- `mvn test` verifies the 150g rice example calculation.
- For a database-backed walkthrough, create an account, add a food with per-100g nutrition, log a quantity, inspect the daily report, set targets, and export the report.

