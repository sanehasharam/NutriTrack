# NutriTrack

A Java 21 menu-driven prototype for food management and daily nutrition tracking, using MySQL and JDBC.

## Requirements

- JDK 21
- Maven 3.9+
- MySQL 8+

## Database setup

Run `database/schema.sql` with a MySQL account that can create a database and tables. Configure the application using:

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

The first menu supports account registration and login. After login, use the food CRUD menu, consumption logging, daily reports, nutrition targets, and CSV export. Nutrition values are stored per 100 grams, and consumed quantities are calculated proportionally. Food records referenced by consumption history cannot be deleted because of the database foreign key.

Passwords are stored as salted PBKDF2 hashes. JDBC statements use parameters and resources use try-with-resources. The daily report can show past dates in `YYYY-MM-DD` format; press Enter for today.

## CA-3 implementation and test checklist

- User registration and login; food create/read/search/update/delete.
- Consumption records associate a user, food, meal category, quantity, and timestamp.
- Daily calories and macros are calculated from the food's per-100g values; targets include remaining intake.
- `mvn test` verifies the 150g rice example calculation.
- For a database-backed walkthrough, create an account, add a food with per-100g nutrition, log a quantity, inspect the daily report, set targets, and export the report.

