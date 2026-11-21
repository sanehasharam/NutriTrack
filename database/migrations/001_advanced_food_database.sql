USE nutritrack;

ALTER TABLE foods
    ADD COLUMN cup_grams DECIMAL(10, 2) NULL,
    ADD COLUMN piece_grams DECIMAL(10, 2) NULL,
    ADD COLUMN serving_grams DECIMAL(10, 2) NULL,
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD INDEX idx_foods_category_active (category, is_active),
    ADD INDEX idx_foods_protein_active (protein_per_100g, is_active),
    ADD INDEX idx_foods_calories_active (calories_per_100g, is_active);
