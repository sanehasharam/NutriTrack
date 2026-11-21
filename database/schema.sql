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
    fat_per_100g DECIMAL(10, 2) NOT NULL
);

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
