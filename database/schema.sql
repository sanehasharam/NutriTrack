CREATE DATABASE IF NOT EXISTS nutritrack;
USE nutritrack;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS foods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(60) NOT NULL,
    calories_per_100g DECIMAL(10, 2) NOT NULL,
    protein_per_100g DECIMAL(10, 2) NOT NULL,
    carbohydrates_per_100g DECIMAL(10, 2) NOT NULL,
    fat_per_100g DECIMAL(10, 2) NOT NULL,
    cup_grams DECIMAL(10, 2),
    piece_grams DECIMAL(10, 2),
    serving_grams DECIMAL(10, 2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_foods_category_active (category, is_active),
    INDEX idx_foods_protein_active (protein_per_100g, is_active),
    INDEX idx_foods_calories_active (calories_per_100g, is_active)
);

INSERT INTO foods (
    name, category, calories_per_100g, protein_per_100g, carbohydrates_per_100g, fat_per_100g,
    cup_grams, piece_grams, serving_grams
)
SELECT seed.name, seed.category, seed.calories, seed.protein, seed.carbohydrates, seed.fat,
       seed.cup_grams, seed.piece_grams, seed.serving_grams
FROM (
    SELECT 'Apple, raw' AS name, 'Fruits' AS category, 52.00 AS calories, 0.26 AS protein,
           13.81 AS carbohydrates, 0.17 AS fat, 125.00 AS cup_grams, 182.00 AS piece_grams,
           182.00 AS serving_grams
    UNION ALL SELECT 'Banana, raw', 'Fruits', 89.00, 1.09, 22.84, 0.33, 150.00, 118.00, 118.00
    UNION ALL SELECT 'Mango, raw', 'Fruits', 60.00, 0.82, 15.00, 0.38, 165.00, 200.00, 165.00
    UNION ALL SELECT 'Papaya, raw', 'Fruits', 43.00, 0.47, 10.82, 0.26, 145.00, NULL, 145.00
    UNION ALL SELECT 'Orange, raw', 'Fruits', 47.00, 0.94, 11.75, 0.12, 180.00, 131.00, 131.00
    UNION ALL SELECT 'Guava, raw', 'Fruits', 68.00, 2.55, 14.32, 0.95, 165.00, 100.00, 100.00
    UNION ALL SELECT 'Tomato, raw', 'Vegetables', 18.00, 0.88, 3.89, 0.20, 180.00, 123.00, 123.00
    UNION ALL SELECT 'Spinach, raw', 'Vegetables', 23.00, 2.86, 3.63, 0.39, 30.00, NULL, 30.00
    UNION ALL SELECT 'Potato, boiled', 'Vegetables', 87.00, 1.87, 20.13, 0.10, 150.00, 173.00, 173.00
    UNION ALL SELECT 'Onion, raw', 'Vegetables', 40.00, 1.10, 9.34, 0.10, 160.00, 110.00, 110.00
    UNION ALL SELECT 'Carrot, raw', 'Vegetables', 41.00, 0.93, 9.58, 0.24, 128.00, 61.00, 61.00
    UNION ALL SELECT 'Cucumber, raw', 'Vegetables', 15.00, 0.65, 3.63, 0.11, 104.00, 300.00, 104.00
    UNION ALL SELECT 'Cauliflower, boiled', 'Vegetables', 23.00, 1.84, 4.11, 0.45, 124.00, NULL, 124.00
    UNION ALL SELECT 'Milk, whole', 'Dairy', 61.00, 3.15, 4.80, 3.25, 244.00, NULL, 244.00
    UNION ALL SELECT 'Curd, plain whole-milk', 'Dairy', 61.00, 3.47, 4.66, 3.25, 245.00, NULL, 150.00
    UNION ALL SELECT 'Paneer', 'Dairy', 265.00, 18.30, 1.20, 20.80, 122.00, NULL, 100.00
    UNION ALL SELECT 'Buttermilk, plain', 'Dairy', 40.00, 3.31, 4.79, 0.88, 245.00, NULL, 245.00
    UNION ALL SELECT 'Rice, white cooked', 'Grains', 130.00, 2.69, 28.17, 0.28, 158.00, NULL, 158.00
    UNION ALL SELECT 'Rice, brown cooked', 'Grains', 123.00, 2.74, 25.58, 0.97, 195.00, NULL, 195.00
    UNION ALL SELECT 'Roti, whole wheat', 'Grains', 297.00, 9.00, 46.00, 7.50, NULL, 40.00, 40.00
    UNION ALL SELECT 'Idli', 'Grains', 58.00, 2.00, 12.00, 0.40, NULL, 40.00, 40.00
    UNION ALL SELECT 'Dosa, plain', 'Grains', 168.00, 4.00, 27.00, 4.00, NULL, 100.00, 100.00
    UNION ALL SELECT 'Poha, cooked', 'Grains', 130.00, 2.50, 23.00, 3.00, 180.00, NULL, 180.00
    UNION ALL SELECT 'Upma, cooked', 'Grains', 160.00, 4.00, 25.00, 5.00, 200.00, NULL, 200.00
    UNION ALL SELECT 'Toor dal, cooked', 'Grains', 120.00, 6.80, 20.20, 0.40, 200.00, NULL, 200.00
    UNION ALL SELECT 'Chickpeas, boiled', 'Grains', 164.00, 8.86, 27.42, 2.59, 164.00, NULL, 164.00
    UNION ALL SELECT 'Rajma, cooked', 'Grains', 127.00, 8.67, 22.80, 0.50, 177.00, NULL, 177.00
    UNION ALL SELECT 'Chicken breast, cooked', 'Protein', 165.00, 31.02, 0.00, 3.57, 140.00, NULL, 120.00
    UNION ALL SELECT 'Egg, hard-boiled', 'Protein', 155.00, 12.58, 1.12, 10.61, NULL, 50.00, 50.00
    UNION ALL SELECT 'Rohu fish, cooked', 'Protein', 150.00, 22.00, 0.00, 6.00, NULL, NULL, 150.00
    UNION ALL SELECT 'Mutton, cooked lean', 'Protein', 250.00, 25.00, 0.00, 16.00, NULL, NULL, 100.00
    UNION ALL SELECT 'Samosa, vegetable', 'Fast Food', 262.00, 5.50, 30.00, 13.00, NULL, 100.00, 100.00
    UNION ALL SELECT 'Vada pav', 'Fast Food', 300.00, 8.00, 43.00, 11.00, NULL, 180.00, 180.00
    UNION ALL SELECT 'Pav bhaji', 'Fast Food', 150.00, 4.00, 21.00, 5.50, 240.00, NULL, 300.00
    UNION ALL SELECT 'Vegetable biryani', 'Fast Food', 150.00, 4.00, 25.00, 4.00, 200.00, NULL, 250.00
    UNION ALL SELECT 'Chai with milk and sugar', 'Beverages', 50.00, 0.90, 7.50, 1.50, 240.00, NULL, 240.00
    UNION ALL SELECT 'Coconut water', 'Beverages', 19.00, 0.72, 3.71, 0.20, 240.00, NULL, 240.00
    UNION ALL SELECT 'Lassi, sweet', 'Beverages', 75.00, 2.50, 12.00, 1.50, 245.00, NULL, 245.00
    UNION ALL SELECT 'Sugarcane juice', 'Beverages', 40.00, 0.10, 9.80, 0.20, 240.00, NULL, 240.00
) AS seed
LEFT JOIN foods AS existing ON existing.name = seed.name
WHERE existing.id IS NULL;

CREATE TABLE IF NOT EXISTS daily_targets (
    user_id BIGINT PRIMARY KEY,
    calories DECIMAL(10, 2) NOT NULL,
    protein DECIMAL(10, 2) NOT NULL,
    carbohydrates DECIMAL(10, 2) NOT NULL,
    fat DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_targets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS consumption_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    meal_category ENUM('BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS') NOT NULL,
    quantity_grams DECIMAL(10, 2) NOT NULL,
    consumed_at DATETIME NOT NULL,
    CONSTRAINT fk_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_logs_food FOREIGN KEY (food_id) REFERENCES foods(id),
    INDEX idx_logs_user_date (user_id, consumed_at)
);
